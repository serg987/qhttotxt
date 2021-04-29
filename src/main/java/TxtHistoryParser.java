import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;

public class TxtHistoryParser {

    private static Chat chat;
    private static FileChannel fileChannel;
    private static Path path;
    private static TxtHistoryParser.txtHistoryType historyType;
    /*
        known 3 formats of files:
        qip/icq: format:
{file begins}
-------------------------------------->-
{Owner_nick} (21:48:51 18/08/2015)
message

--------------------------------------<-
{contact_nick} (14:28:50 19/10/2015)
Message

        mchat: format:
{file begins}
28.05.08 10:13:17<message
28.05.08 10:15:26>message
28.05.08 10:21:39<message
message str2
28.05.08 10:21:59>message

           RnQ (converted with some history viewer)
{file begins}
Чат между {Owner_uin} и {contactUin}


13.10.2006 10:48:00 {contactUin}
message


13.10.2006 10:48:40 {Owner_uin}
message


13.10.2006 10:54:58 {contactUin}
message

     */

    public static HashMap<Path, Chat> parseChatsFromTxt() {
        HashMap<Path, Chat> chats = new HashMap<>();
        List<Path> paths = IOHelper.getPathListTxt();
        for (Path path : paths) {
            List<String> fileLines = IOHelper.convertFileToStrings(path);
            Chat chat = TxtHistoryParser.parseChatFromTxt(path, fileLines);
            if (chat != null) chats.put(path, chat);
        }
        return chats;
    }

    private static Chat parseChatFromTxt(Path pathToSet, List<String> fileLines) {
        path = pathToSet;
        historyType = determineTypeOfTxtHistory(fileLines);
        if (historyType.equals(txtHistoryType.NO_HISTORY)) return null;
        chat = new Chat();
        chat.uin = path.getFileName().toString().toLowerCase().replace(".txt", "");
        chat.nickName = chat.uin; // TODO delete??? after debugging; populating should be by contactlist
        chat.uinLength = chat.uin.length();
        parseChat();
        System.out.println(String.format(Configuration.foundTxtChatWith,
                historyType.name(),
                chat.uin,
                chat.messages.size()));

        return chat;
    }

    private static void parseChat() {
        File file = new File(path.toUri());
        FileInputStream fs = null;
        fileChannel = null;
        try {
            fs = new FileInputStream(file);
            fileChannel = fs.getChannel();
            while (channelAvailableBytes() > 0) {
                switch (historyType) {
                    case MCHAT:
                        parseMchatMessage();
                        break;
                    case QIP_ICQ:
                        parseQipMessage();
                        break;
                    case RNQ:
                        parseRnqMessage();
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileChannel != null) fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fs != null) fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void parseQipMessage() throws IOException {
        String currentString = Commons.guessCodePageAndConvertIfNeeded(readBytesOfLineFromChannel());
        Matcher m = Configuration.qipIcqSeparatorPattern.matcher(currentString);
        if (m.find()) {
            Message message = new Message();
            char messageDirection = currentString.charAt(currentString.length() - 2);
            if (messageDirection == '>') message.isSent = true;
            String header = Commons.guessCodePageAndConvertIfNeeded(readBytesOfLineFromChannel());
            String headerTime = header.split("\\(")[1].replace(")", "");
            message.unixDate = Commons.parseDateTime(headerTime);
            message.messageText = Commons.guessCodePageAndConvertIfNeeded(readBytesOfLineFromChannel());
            boolean foundAnotherString = true;
            while (channelAvailableBytes() > 0 && foundAnotherString) {
                List<String> nextTwoLines = tryNextNLines(2);
                if (nextTwoLines.size() < 2) {
                    foundAnotherString = false;
                    continue;
                }
                m = Configuration.qipIcqSeparatorPattern.matcher(nextTwoLines.get(1));
                if (!m.find()) {
                    message.addLineToMessageText(
                            Commons.guessCodePageAndConvertIfNeeded(readBytesOfLineFromChannel()));
                } else {
                    foundAnotherString = false;
                    readBytesOfLineFromChannel();
                }
            }
            chat.messages.add(message);
        }
    }

    private static void parseRnqMessage() throws IOException {
        String currentString = Commons.guessCodePageAndConvertIfNeeded(readBytesOfLineFromChannel());
        Matcher m = Configuration.rnqLineHeaderPattern.matcher(currentString);
        if (m.find()) {
            Message message = new Message();
            String[] headerWords = currentString.split(" ");
            String uinInMessage = headerWords[2];
            if (!uinInMessage.equals(chat.uin)) message.isSent = true;
            message.unixDate = Commons.parseDateTime(headerWords[0] + " " + headerWords[1]);
            message.messageText = Commons.guessCodePageAndConvertIfNeeded(readBytesOfLineFromChannel());
            boolean foundAnotherString = true;
            while (channelAvailableBytes() > 0 && foundAnotherString) {
                List<String> nextThreeLines = tryNextNLines(3);
                if (nextThreeLines.size() < 3) {
                    foundAnotherString = false;
                    continue;
                }
                m = Configuration.rnqLineHeaderPattern.matcher(nextThreeLines.get(2));
                if (!m.find()) {
                    message.addLineToMessageText(Commons
                            .guessCodePageAndConvertIfNeeded(readBytesOfLineFromChannel()));
                } else {
                    foundAnotherString = false;
                    readBytesOfLineFromChannel();
                    readBytesOfLineFromChannel();
                }
            }
            chat.messages.add(message);
        }
    }

    private static void parseMchatMessage() throws IOException {
        byte[] lineBytes = readBytesOfLineFromChannel();
        String currentString = Commons.guessCodePageAndConvertIfNeeded(lineBytes);
        Matcher m = Configuration.mchatLineHeaderPattern.matcher(currentString);
        if (m.find()) {
            Message message = new Message();
            String lineHeader = m.group();
            int headerLength = lineHeader.length();
            char messageDirection = lineHeader.charAt(lineHeader.length() - 1);
            if (messageDirection == '>') message.isSent = true;
            lineHeader = lineHeader.replace("" + messageDirection, "");
            message.unixDate = Commons.parseDateTime(lineHeader);
            byte[] lineBytesWithoutHeader = Arrays.copyOfRange(lineBytes, headerLength, lineBytes.length);
            message.messageText = Commons.guessCodePageAndConvertIfNeeded(lineBytesWithoutHeader);
            boolean foundAnotherString = true;
            while (channelAvailableBytes() > 0 && foundAnotherString) {
                String nextStr = tryNextString();
                m = Configuration.mchatLineHeaderPattern.matcher(nextStr);
                if (!m.find()) {
                    message.addLineToMessageText(Commons
                            .guessCodePageAndConvertIfNeeded(readBytesOfLineFromChannel()));
                } else {
                    foundAnotherString = false;
                }
            }
            chat.messages.add(message);
        }
    }

    private static List<String> tryNextNLines(int n) throws IOException {
        List<String> out = new ArrayList<>();
        long previousPosition = fileChannel.position();
        while (channelAvailableBytes() > 0 && n > 0) {
            String str = Commons.guessCodePageAndConvertIfNeeded(readBytesOfLineFromChannel());
            out.add(str);
            n--;
        }
        fileChannel.position(previousPosition);
        return out;
    }

    private static byte[] readBytesOfLineFromChannel() throws IOException {
        long previousPosition = fileChannel.position();
        boolean eolFound = false;
        ByteBuffer buffer;
        while (channelAvailableBytes() > 0 && !eolFound) {
            buffer = ByteBuffer.allocate(1);
            fileChannel.read(buffer);
            buffer.position(0);
            if (buffer.get() == Configuration.newLineBytes[0]) {
                fileChannel.position(fileChannel.position() - 1);
                buffer = ByteBuffer.allocate(Configuration.newLineBytes.length);
                fileChannel.read(buffer);
                buffer.position(0);
                if (Arrays.equals(buffer.array(), Configuration.newLineBytes)) {
                    eolFound = true;
                } else {
                    fileChannel.position(fileChannel.position() - Configuration.newLineBytes.length + 1);
                }
            }
        }
        long newPosition = fileChannel.position();
        int lineLength = (int) (newPosition - previousPosition);
        if (eolFound) {
            lineLength -= Configuration.newLineBytes.length;
        }
        buffer = ByteBuffer.allocate(lineLength);
        fileChannel.position(previousPosition);
        fileChannel.read(buffer);
        buffer.position(0);
        fileChannel.position(newPosition);
        return buffer.array();
    }

    private static String tryNextString() throws IOException {
        long previousPosition = fileChannel.position();
        String out = Commons.guessCodePageAndConvertIfNeeded(readBytesOfLineFromChannel());
        fileChannel.position(previousPosition);
        return out;
    }

    private static txtHistoryType determineTypeOfTxtHistory(List<String> fileLines) {
        for (int i = 0; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            if (line.isEmpty()) continue;
            if (line.matches(Configuration.qip_icq_separator)) {
                if (i + 1 < fileLines.size()) {
                    if (fileLines.get(i + 1).matches(Configuration.qip_icq_timeline)) return txtHistoryType.QIP_ICQ;
                }
            }
            if (line.matches(Configuration.mchat_line)) return txtHistoryType.MCHAT;
            if (line.matches(Configuration.rnq_line)) return txtHistoryType.RNQ;
        }


        return txtHistoryType.NO_HISTORY;
    }

    private static int channelAvailableBytes() throws IOException {
        return (int) (fileChannel.size() - fileChannel.position());
    }

    enum txtHistoryType {
        NO_HISTORY,
        QIP_ICQ,
        MCHAT,
        RNQ
    }
}
