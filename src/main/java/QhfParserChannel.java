import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class QhfParserChannel {
    private static File file;
    private static FileChannel fileChannel;
    private static long previousChannelPosition;

    public static Chat parseQhfFile(Path path) throws IOException {
        Chat chat = null;
        FileInputStream fs = null;
        // all the thing with FileChannel b/c we need to go backwards for handling corrupted messages
        // otherwise FileInputStream would be enough
        fileChannel = null;
        try {
            chat = new Chat();
            file = new File(path.toUri());
            fs = new FileInputStream(file);
            fileChannel = fs.getChannel();
            chat.header = readChars(0, 3);
            if (!chat.header.equals("QHF"))
                throw new IOException(String.format(Configuration.notQhfFile, file.getName()));
            chat.historySize = readInt32(1);
            chat.numberOfMsgs = readInt32(26);
            chat.numberOfMsgs2 = readInt32(0);
            chat.uinLength = readInt16(2);
            chat.uin = readChars(0, chat.uinLength);
            chat.nickNameLength = readInt16(0);
            chat.nickName = readChars(0, chat.nickNameLength);

            while (fs.available() > 6) {
                chat.messages.add(parseMessage());
            }
        } catch (IOException e) {
            if (chat.messages.size() > 0) {
                saveCorruptedChat(path, chat);
            } else throw e;
        } finally {
            if (fileChannel != null) fileChannel.close();
            if (fs != null) fs.close();
        }

        return chat;
    }

    private static void saveCorruptedChat(Path path, Chat chat) throws IOException {
        System.out.println(String.format(Configuration.tryingToSaveCorruptedFile,
                path.getFileName().toAbsolutePath().toString(),
                chat.messages.size(), chat.numberOfMsgs));
        String txtFileName = path.getFileName().toString()
                .replace(".qhf", "").replace(".ahf", "")
                .concat("_DAMAGED.txt");
        saveChatToTxt(chat, Paths.get(path.getParent().toString(), txtFileName));
    }

    protected static Message parseMessage() throws IOException { // TODO change to private after testing
        Message m = new Message();
        if (readInt16(0) != 1) {
            throw new IOException(String.format(Configuration.cannotReadMsg, file.getAbsolutePath()));
        }
        fillMessageData(m);
        // sometimes there are messages with 0 length. handle it properly
        if (m.msgBlockSize == 27) {
            m.setMessageByteArray(
                    Configuration.messageWithZeroLength.getBytes(Configuration.defaultEncoding));
            return m;
        }
        if ((m.msgBlockSize - m.messageLength) != 27) {
            // encoded message - go back and read Int32
            fileChannel.position(previousChannelPosition);
            m.messageLength = readInt32(0);
            if (m.messageLength == 0) {
                m.setMessageByteArray(
                        Configuration.messageWithZeroLength.getBytes(Configuration.defaultEncoding));
                return m;
            }
            m.isEncoded = true;
        }

        ByteBuffer buffer = ByteBuffer.allocate(m.messageLength);
        fileChannel.read(buffer);
        m.setMessageByteArray(buffer.array());

        // checking if the message is corrupted and trying to fix it
        if (channelAvailableBytes() > 1 && readInt16(0) != 1) {
            // corrupted
            int corruptedBytesNum = 0;
            boolean properHeaderFound = false;
            // if less than 34 bytes left, there is no sense to check - the header is corrupted
            while (channelAvailableBytes() > 34 && !properHeaderFound) {
                fileChannel.position(previousChannelPosition + 1);
                corruptedBytesNum++;
                if (readInt16(0) == 1) {
                    fileChannel.position(previousChannelPosition);
                    int typeOfId = readInt16(6);
                    fileChannel.position(previousChannelPosition);
                    int idBlockSize = readInt16(8);
                    fileChannel.position(previousChannelPosition);
                    int sendingDateFieldSize = readInt16(16);
                    fileChannel.position(previousChannelPosition);
                    properHeaderFound = (typeOfId == 1 && idBlockSize == 4 && sendingDateFieldSize == 4);
                }
            }
            m.corruptedBytesNum = corruptedBytesNum;
        }
            if (channelAvailableBytes() > 1) fileChannel.position(previousChannelPosition);
        return m;
    }

    private static void fillMessageData(Message m) throws IOException {
        m.msgBlockSize = readInt32(0);
        m.tOMsgFieldId = readInt16(0);
        m.idBlockSize = readInt16(0);
        m.id = readInt32(0);
        m.typeOfSendingDateField = readInt16(0);
        m.sendingDateFieldSize = readInt16(0);
        m.unixDate = readInt32(0);
        m.typeOfFieldUnknown = readInt16(0);
        m.typeOfFieldUnknown2 = readInt16(0);
        m.isSent = readByte(0) > 0;
        m.setTypeOfMsgField((byte) readInt16(0));
        m.messageLengthBlockSize = readInt16(0);
        m.messageLength = readInt16(0);
    }

    public static void saveChatToTxt(Chat chat, Path path) throws IOException {
        File fileToSave = new File(path.toUri());
        FileOutputStream outputStream = new FileOutputStream(fileToSave);

        try {
            StringBuilder stringBuilder = new StringBuilder();

            for (Message m : chat.messages) {
                ZonedDateTime zonedDateTime = Instant.ofEpochSecond(m.unixDate).atZone(Configuration.zoneId);
                stringBuilder.append("--------------------------------------")
                        .append(m.isSent ? ">" : "<").append("-");
                addCRtoStringBuilder(stringBuilder);
                if (m.corruptedBytesNum > 0) {
                    stringBuilder.append(String.format(Configuration.messageIsCorrupted, m.corruptedBytesNum));
                    addCRtoStringBuilder(stringBuilder);
                }
                stringBuilder.append(m.isSent ? Configuration.ownNickName : chat.nickName)
                        .append(" (")
                        .append(zonedDateTime.format(DateTimeFormatter.ofPattern(Configuration.timePatternInTxt)))
                        .append(")");
                addCRtoStringBuilder(stringBuilder);
                outputStream.write(stringBuilder.toString().getBytes(Configuration.defaultEncoding));
                stringBuilder.setLength(0);
                outputStream.write(m.getMessageByteArray());
                addCRtoStringBuilder(stringBuilder);
                addCRtoStringBuilder(stringBuilder);
                outputStream.write(stringBuilder.toString().getBytes(Configuration.defaultEncoding));
                stringBuilder.setLength(0);
            }

            outputStream.flush();
        } catch (UnsupportedEncodingException e) {
            System.out.println(Configuration.getNoCodepageFound());
            e.printStackTrace();
        } finally {
            outputStream.close();
        }
    }

    private static void addCRtoStringBuilder(StringBuilder stringBuilder) {
        stringBuilder.append(System.getProperty("line.separator"));
    }


    private static ByteBuffer allocateByteBufferReadAndResetPosition(int bytesToRead) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(bytesToRead);
        fileChannel.read(buffer);
        buffer.position(0);
        return buffer;
    }

    private static byte readByte(int offset) throws IOException {
        storePreviousChannelPosition();
        fileChannel.position(previousChannelPosition + offset);
        if (channelAvailableBytes() > offset + 1) {
            return allocateByteBufferReadAndResetPosition(1).get();
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static int readInt32(int offset) throws IOException {
        storePreviousChannelPosition();
        fileChannel.position(previousChannelPosition + offset);
        if (channelAvailableBytes() > 3) {
            return allocateByteBufferReadAndResetPosition(4).getInt();
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static int readInt16(int offset) throws IOException {
        storePreviousChannelPosition();
        fileChannel.position(previousChannelPosition + offset);
        if (channelAvailableBytes() > 1) {
            return allocateByteBufferReadAndResetPosition(2).getShort();
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static String readChars(int offset, int length) throws IOException {
        storePreviousChannelPosition();
        fileChannel.position(previousChannelPosition + offset);
        if (channelAvailableBytes() >= length) {
            ByteBuffer buffer = ByteBuffer.allocate(length);
            fileChannel.read(buffer);
            return new String(buffer.array(), Configuration.defaultEncoding).trim();
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static void storePreviousChannelPosition() throws IOException {
        previousChannelPosition = fileChannel.position();
    }

    private static int channelAvailableBytes() throws IOException {
        return (int) (fileChannel.size() - fileChannel.position());
    }
}
