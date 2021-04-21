import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class QhfParserChannel {
    private static File file;
    private static FileChannel fileChannel;
    private static long currentChannelPosition;

    public static Chat parseQhfFile(Path path) throws IOException {
        Chat chat = null;
        FileInputStream fs = null;
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
            throw e;
        } finally {
            if (fileChannel != null) fileChannel.close();
            if (fs != null) fs.close();
        }

        return chat;
    }

    protected static Message parseMessage() throws IOException { // TODO change to private after testing
        Message message = new Message();
        if (readInt16(0) != 1) {
            throw new IOException(String.format(Configuration.cannotReadMsg, file.getAbsolutePath()));
        }
        message.msgBlockSize = readInt32(0);
        message.setTOMsgFieldId((byte) readInt16(0));
        message.idBlockSize = readInt16(0);
        message.id = readInt32(0);
        message.typeOfSendingDateField = readInt16(0);
        message.sendingDateFieldSize = readInt16(0);
        message.unixDate = readInt32(0);
        message.typeOfFieldUnknown = readInt16(0);
        message.typeOfFieldUnknown2 = readInt16(0);
        message.isSent = readByte(0) > 0;
        message.typeOfMessageField = readInt16(0);
        message.messageLengthBlockSize = readInt16(0);
        message.messageLength = readInt16(0);
        // sometimes there are messages with 0 length. handle it properly
        if (message.msgBlockSize == 27) {
            message.setMessageByteArray(
                    Configuration.messageWithZeroLength.getBytes(Configuration.defaultEncoding));
            return message;
        }
        // switching between encoded/plain message; because of that there is no support
        // for encoded messages > 65Kb
        if (message.messageLength == 0) {
            message.messageLength = readInt16(0);
            // handle properly encoded messages with 0 length
            if (message.messageLength == 0) {
                message.setMessageByteArray(
                        Configuration.messageWithZeroLength.getBytes(Configuration.defaultEncoding));
                return message;
            }
            message.isEncoded = true;
        }

        ByteBuffer buffer = ByteBuffer.allocate(message.messageLength);
        fileChannel.read(buffer);
        message.setMessageByteArray(buffer.array());

        return message;
    }

    public static void saveChatToTxt(Chat chat, Path path) throws IOException {
        File fileToSave = new File(path.toUri());
        FileOutputStream outputStream = new FileOutputStream(fileToSave);

        try {
            StringBuilder stringBuilder = new StringBuilder();

            for (Message message : chat.messages) {
                ZonedDateTime zonedDateTime = Instant.ofEpochSecond(message.unixDate).atZone(Configuration.zoneId);
                stringBuilder.append("--------------------------------------")
                        .append(message.isSent ? ">" : "<").append("-");
                addCRtoStringBuilder(stringBuilder);
                stringBuilder.append(message.isSent ? Configuration.ownNickName : chat.nickName)
                        .append(" (")
                        .append(zonedDateTime.format(DateTimeFormatter.ofPattern(Configuration.timePatternInTxt)))
                        .append(")");
                addCRtoStringBuilder(stringBuilder);
                outputStream.write(stringBuilder.toString().getBytes(Configuration.defaultEncoding));
                stringBuilder.setLength(0);
                outputStream.write(message.getMessageByteArray());
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
        storeCurrentChannelPosition();
        fileChannel.position(currentChannelPosition + offset);
        //getOffsetInFileStream(fs, offset);
        if (channelAvailableBytes() > offset + 1) {
            return allocateByteBufferReadAndResetPosition(1).get();
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static int readInt32(int offset) throws IOException {
        storeCurrentChannelPosition();
        fileChannel.position(currentChannelPosition + offset);
        //getOffsetInFileStream(fs, offset);
        int out;
        if (channelAvailableBytes() > 3) {

/*            byte[] b = buffer.array();
            out = b[0] & 0xFF;
            out = out << 8;
            out += b[1] & 0xFF;
            out = out << 8;
            out += b[2] & 0xFF;
            out = out << 8;
            out += b[3] & 0xFF;*/
            return allocateByteBufferReadAndResetPosition(4).getInt();
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static int readInt16(int offset) throws IOException {
        storeCurrentChannelPosition();
        fileChannel.position(currentChannelPosition + offset);
        //getOffsetInFileStream(fs, offset);
        int out;
        if (channelAvailableBytes() > 1) {

            //byte[] b = buffer.array();
            //out = b[0] & 0xFF;
            //out = out << 8;
            //out += b[1] & 0xFF;
            return allocateByteBufferReadAndResetPosition(2).getShort();
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static String readChars(int offset, int length) throws IOException {
        storeCurrentChannelPosition();
        fileChannel.position(currentChannelPosition + offset);
        //getOffsetInFileStream(fs, offset);
        if (channelAvailableBytes() >= length) {
            ByteBuffer buffer = ByteBuffer.allocate(length);
            fileChannel.read(buffer);
            return new String(buffer.array(), Configuration.defaultEncoding).trim();
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static void storeCurrentChannelPosition() throws IOException {
        currentChannelPosition = fileChannel.position();
    }

    private static void getOffsetInFileStream(FileInputStream fs, int offset) throws IOException {
        if (offset == 0) return;
        if (channelAvailableBytes() >= offset) {
            byte[] b = new byte[offset];
            fs.read(b);
            return;
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static void checkChannel(FileInputStream fs) {
        FileChannel channel = fs.getChannel();
       /// channel.
    }

    private static int channelAvailableBytes() throws IOException {
        return (int) (fileChannel.size() - fileChannel.position());
    }
}
