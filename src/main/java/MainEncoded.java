import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class MainEncoded {

    public static void main(String[] args) {
        Chat chat = new Chat();
        try {
            File file = new File("E:\\!Temp\\210420\\Issues\\!ICQ_295157315_406823170.qhf");
            chat = QhfParserChannel.parseQhfFile(file.toPath());
            // fs.read(); // the header is over! // changed
            System.out.println("header=" + chat.header);
            System.out.println("historySize=" + chat.historySize);
            System.out.println("numberOfMsgs=" + chat.numberOfMsgs);
            System.out.println("numberOfMsgs2=" + chat.numberOfMsgs2);
            System.out.println("uinLength=" + chat.uinLength);
            System.out.println("uin=" + chat.uin);
            System.out.println("nickNameLength=" + chat.nickNameLength);
            System.out.println("nickName=" + chat.nickName);
            System.out.println();

            /*Message message = parseMessage(fs);
            messages.add(message);
            System.out.println("message time=" + (Instant.ofEpochSecond(message.unixDate).atZone(zoneId).toString()));
            ZonedDateTime zonedDateTime = Instant.ofEpochSecond(message.unixDate).atZone(zoneId);
            System.out.println(zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")));*/
/*            int j = 0;
            while (fs.available() > 6) {
                System.out.println("Message " + j);
                chat.messages.add(QhfParserChannel.parseMessage(fs));
                j++;
            }*/

            System.out.println("Messages: " + chat.messages.size());


            QhfParser.saveChatToTxt(chat, Paths.get("C:\\Users\\Testing-Coding\\IdeaProjects\\qhftotxt\\src\\main\\resources\\testfiles\\4.txt"));
            //saveTxtFile3(chat);
            //  messages.stream().forEach(m -> System.out.println(m.message));

            Message message = chat.messages.get(0);

            System.out.println("message.msgBlockSize=" + message.msgBlockSize);
            System.out.println("message.tOMsgFieldId=" + message.tOMsgFieldId.name());
            System.out.println("message.idBlockSize=" + message.idBlockSize);
            System.out.println("message.id=" + message.id);
            System.out.println("message.typeOfSendingDateField=" + message.typeOfSendingDateField);
            System.out.println("message.sendingDateFieldSize=" + message.sendingDateFieldSize);
            System.out.println("message.unixDate=" + message.unixDate);
            System.out.println("message time=" + (new Date((long)message.unixDate * 1000).toString()));
            System.out.println("message.typeOfFieldUnknown=" + message.typeOfFieldUnknown);
            System.out.println("message.typeOfFieldUnknown2=" + message.typeOfFieldUnknown2);
            System.out.println("message.isSent=" + message.isSent);
            System.out.println("message.typeOfMessageField=" + message.typeOfMessageField);
            System.out.println("message.messageLengthBlockSize=" + message.messageLengthBlockSize);
            System.out.println("message.messageLength=" + message.messageLength);
            System.out.println("message.message=" + message.getMessageByteArray().toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void saveTxtFile3(Chat chat) throws IOException {
        File file1 = new File("C:\\Users\\Testing-Coding\\IdeaProjects\\qhftotxt\\src\\main\\resources\\4.txt");
        FileOutputStream outputStream = new FileOutputStream(file1);

        try {

            StringBuilder stringBuilder = new StringBuilder();

            for (Message message : chat.messages) {
                System.out.println("message.msgBlockSize=" + message.msgBlockSize
                        + "; message.messageLength=" + message.messageLength + "; difference=" + (message.msgBlockSize - message.messageLength));
                ZonedDateTime zonedDateTime = Instant.ofEpochSecond(message.unixDate).atZone(Configuration.zoneId);
                stringBuilder.append("--------------------------------------")
                        .append(message.isSent ? ">" : "<").append("-");
                addCRtoStringBuilder(stringBuilder);
                stringBuilder.append(message.isSent ? Configuration.ownNickName : chat.nickName)
                        .append(" (")
                        .append(zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")))
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
        } catch (Exception e) {
            throw e;
        } finally {
            outputStream.close();
        }

    }


    private static void addCRtoStringBuilder(StringBuilder stringBuilder) {
        stringBuilder.append(System.getProperty("line.separator"));
    }

    private static Message parseMessage(FileInputStream fs) throws IOException {
        Message message = new Message();
        if (readInt16(fs, 0) != 1) {
          //  String aaa = "jjj";
            throw new IOException("File is corrupted! Cannot read message");
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
        // handling encoded/plain message; because of that there is no support
        // for encoded messages > 65Kb
        if (message.messageLength == 0) {
            message.isEncoded = true;
            message.messageLength = readInt16(fs, 0);
        }
        byte[] messageStringEncoded = new byte[message.messageLength];
        fs.read(messageStringEncoded);
        message.setMessageByteArray(messageStringEncoded);

        return message;
    }

    private static String decodeMessage(byte[] bytes) {
        String out = "";
        try {
            out = new String(bytes, Configuration.defaultEncoding);
        } catch (UnsupportedEncodingException e) {
            out = "[COULD NOT DECODE MESSAGE, TRY USE DIFFERENT CHARSET]";
            e.printStackTrace();
        }

        return out;
    }

    private static byte readByte(FileInputStream fs, int offset) throws IOException {
        getOffsetInFileStream(fs, offset);
        if (fs.available() > offset + 1) {
            return (byte) fs.read();
        }
        throw new IOException("No bytes available");
    }

    private static int readInt32(FileInputStream fs, int offset) throws IOException {
        getOffsetInFileStream(fs, offset);
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
        throw new IOException("No bytes available");
    }

    private static int readInt16(FileInputStream fs, int offset) throws IOException {
        getOffsetInFileStream(fs, offset);
        int out;
        if (fs.available() > 1) {
            byte[] b = new byte[2];
            fs.read(b);
            out = b[0] & 0xFF;
            out = out << 8;
            out += b[1] & 0xFF;
            return out;
        }
        throw new IOException("No bytes available");
    }

    private static String readChars(FileInputStream fs, int offset, int length) throws IOException {
        getOffsetInFileStream(fs, offset);
        if (fs.available() >= length) {
            byte[] b = new byte[length];
            fs.read(b);
            return new String(b, Configuration.defaultEncoding);
        }
        throw new IOException("No bytes available");
    }

    private static void getOffsetInFileStream(FileInputStream fs, int offset) throws IOException {
        if (offset == 0) return;
        if (fs.available() >= offset) {
            byte[] b = new byte[offset];
            fs.read(b);
            return;
        }
        throw new IOException("No bytes available");
    }

}
