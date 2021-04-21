import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class QhfParser {
    private static File file;

    public static Chat parseQhfFile(Path path) throws IOException {
        Chat chat = null;
        FileInputStream fs = null;
        try {
            chat = new Chat();
            file = new File(path.toUri());
            fs = new FileInputStream(file);
            chat.header = readChars(fs, 0, 3);
            if (!chat.header.equals("QHF"))
                throw new IOException(String.format(Configuration.notQhfFile, file.getName()));
            chat.historySize = readInt32(fs, 1);
            chat.numberOfMsgs = readInt32(fs, 26);
            chat.numberOfMsgs2 = readInt32(fs, 0);
            chat.uinLength = readInt16(fs, 2);
            chat.uin = readChars(fs, 0, chat.uinLength);
            chat.nickNameLength = readInt16(fs, 0);
            chat.nickName = readChars(fs, 0, chat.nickNameLength);

            while (fs.available() > 6) {
                chat.messages.add(parseMessage(fs));
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (fs != null) fs.close();
        }

        return chat;
    }

    protected static Message parseMessage(FileInputStream fs) throws IOException { // TODO change to private after testing
        Message message = new Message();
        if (readInt16(fs, 0) != 1) {
            throw new IOException(String.format(Configuration.cannotReadMsg, file.getAbsolutePath()));
        }
        message.msgBlockSize = readInt32(fs, 0);
        message.setTOMsgFieldId((byte) readInt16(fs, 0));
        message.idBlockSize = readInt16(fs, 0);
        message.id = readInt32(fs, 0);
        message.typeOfSendingDateField = readInt16(fs, 0);
        message.sendingDateFieldSize = readInt16(fs, 0);
        message.unixDate = readInt32(fs, 0);
        message.typeOfFieldUnknown = readInt16(fs, 0);
        message.typeOfFieldUnknown2 = readInt16(fs, 0);
        message.isSent = readByte(fs, 0) > 0;
        message.typeOfMessageField = readInt16(fs, 0);
        message.messageLengthBlockSize = readInt16(fs, 0);
        message.messageLength = readInt16(fs, 0);
        // sometimes there are messages with 0 length. handle it properly
        if (message.msgBlockSize == 27) {
            message.setMessageByteArray(
                    Configuration.messageWithZeroLength.getBytes(Configuration.defaultEncoding));
            return message;
        }
        // switching between encoded/plain message; because of that there is no support
        // for encoded messages > 65Kb
        if (message.messageLength == 0) {
            message.messageLength = readInt16(fs, 0);
            // handle properly encoded messages with 0 length
            if (message.messageLength == 0) {
                message.setMessageByteArray(
                        Configuration.messageWithZeroLength.getBytes(Configuration.defaultEncoding));
                return message;
            }
            message.isEncoded = true;
        }

        byte[] messageStringEncoded = new byte[message.messageLength];
        fs.read(messageStringEncoded);
        message.setMessageByteArray(messageStringEncoded);

        return message;
    }

    public static void saveChatToTxt(Chat chat, Path path) throws IOException {
        File file1 = new File(path.toUri());
        FileOutputStream outputStream = new FileOutputStream(file1);

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


    private static byte readByte(FileInputStream fs, int offset) throws IOException {
        fs.skip(offset);
        //getOffsetInFileStream(fs, offset);
        if (fs.available() > offset + 1) {
            return (byte) fs.read();
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static int readInt32(FileInputStream fs, int offset) throws IOException {
        fs.skip(offset);
        //getOffsetInFileStream(fs, offset);
        int out;
        if (fs.available() > 3) {
            byte[] b = new byte[4];
            fs.read(b);
            out = b[0] & 0xFF;
            out = out << 8;
            out += b[1] & 0xFF;
            out = out << 8;
            out += b[2] & 0xFF;
            out = out << 8;
            out += b[3] & 0xFF;
            return out;
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static int readInt16(FileInputStream fs, int offset) throws IOException {
        fs.skip(offset);
        //getOffsetInFileStream(fs, offset);
        int out;
        if (fs.available() > 1) {
            byte[] b = new byte[2];
            fs.read(b);
            out = b[0] & 0xFF;
            out = out << 8;
            out += b[1] & 0xFF;
            return out;
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static String readChars(FileInputStream fs, int offset, int length) throws IOException {
        fs.skip(offset);
        //getOffsetInFileStream(fs, offset);
        if (fs.available() >= length) {
            byte[] b = new byte[length];
            fs.read(b);
            return new String(b, Configuration.defaultEncoding);
        }
        throw new IOException(String.format(Configuration.noBytesAvailable, file.getAbsolutePath()));
    }

    private static void getOffsetInFileStream(FileInputStream fs, int offset) throws IOException {
        if (offset == 0) return;
        if (fs.available() >= offset) {
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
}
