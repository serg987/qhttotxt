import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TxtHistoryParser {

    private static final String qip_icq_separator_pattern = "^[-]{38}[<>][-]";
    private static final String qip_icq_timeline_pattern = "^[\\d|\\p{L}|\\s|@|\\.]*\\s[(][\\d|:]*\\s[\\d|\\/|\\.]*[)]";
    private static final String mchat_line_header = "^([\\d]{2}[\\/|\\.]){2}[\\d]{2,4}\\s[\\d]{1,2}([:][\\d]{2}){2}[<|>]";
    private static final String mchat_line_pattern = mchat_line_header + ".*";
    private static final String rnq_line_pattern = "^([\\d]{2}[\\/|\\.]){2}[\\d]{2,4}\\s" +
            "([\\d]{2}[:]){2}[\\d]{2}\\s[\\d]{1,12}\\s$";
    private static final byte[] newLineBytes = System.getProperty("line.separator").getBytes();

    private static final Pattern mchatLineHeaderPattern = Pattern.compile(mchat_line_header);
    /*
        known 3 TYPES of files:
        qip/icq: format:
{file begins}
-------------------------------------->-
{Owner_Nick} (21:48:51 18/08/2015)
message

--------------------------------------<-
{contact_Nick} (14:28:50 19/10/2015)
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

    public static List<Chat> parseChatsFromTxt(List<Path> paths) {
        List<Chat> chats = new ArrayList<>();
        Map<Path, List<String>> map = IOHelper.convertFilesToStrings(paths, Charset.forName(Configuration.defaultCodepage));
        for (Path path : map.keySet()) {
            chats.add(TxtHistoryParser.parseChatFromTxt(path, map.get(path)));
        }
        return chats;
    }

    public static Chat parseChatFromTxt(Path path, List<String> fileLines) {
        txtHistoryType chatType = determineTypeOfTxtHistory(fileLines);
        if (chatType.equals(txtHistoryType.NO_HISTORY)) return null;
        Chat chat = null;
        switch (chatType) {
            case MCHAT:
                chat = parseMchatChat(path);
                break;
            case RNQ:
                chat = parseRnqChat(path);
                break;
            case QIP_ICQ:
                chat = parseQipChat(path);
        }
        chat.uin = path.getFileName().toString().toLowerCase().replace(".txt", "");
        chat.nickName = chat.uin; // TODO delete after debugging; populating should be by contactlist
        chat.uinLength = chat.uin.length();

        System.out.println("Found chat with: " + chat.uin + ": messages: " + chat.messages.size());

        return chat;
    }

    private static Chat parseQipChat(Path path) {

        return null;
    }

    private static Chat parseRnqChat(Path path) {

        return null;
    }

    private static Chat parseMchatChat(Path path) {
        Chat chat = new Chat();
        File file = new File(path.toUri());
        FileInputStream fs = null;
        FileChannel fileChannel = null;
        try {
            fs = new FileInputStream(file);
            fileChannel = fs.getChannel();
            while (channelAvailableBytes(fileChannel) > 0) {
                byte[] lineBytes = readBytesOfLineFromChannel(fileChannel);
                String currentString = new String(lineBytes, Configuration.defaultCodepage);
                Matcher m = mchatLineHeaderPattern.matcher(currentString);
                if (m.find()) {
                    Message message = new Message();
                    String lineHeader = m.group();
                    int headerLength = lineHeader.length();
                    char messageDirection = lineHeader.charAt(lineHeader.length() - 1);
                    if (messageDirection == '>') message.isSent = true;
                    lineHeader = lineHeader.replace("" + messageDirection, "");
                    message.unixDate = Commons.parseDateTime(lineHeader);
                    byte[] lineBytesWithoutHeader = Arrays.copyOfRange(lineBytes, headerLength, lineBytes.length);
                    message.setMessageByteArray(lineBytesWithoutHeader);
                    boolean foundAnotherString = true;
                    while (channelAvailableBytes(fileChannel) > 0 && foundAnotherString){
                        String nextStr = tryNextString(fileChannel);
                        m = mchatLineHeaderPattern.matcher(nextStr);
                        if (!m.find()) {
                            message.addLineToMessageByteArray(newLineBytes);
                            message.addLineToMessageByteArray(readBytesOfLineFromChannel(fileChannel));
                        } else {
                            foundAnotherString = false;
                        }
                    }
                    chat.messages.add(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fs != null) fileChannel.close();
                if (fileChannel != null) fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return chat;
    }

    private static byte[] readBytesOfLineFromChannel(FileChannel fileChannel) throws IOException {
        long previousPosition = fileChannel.position();
        boolean eolFound = false;
        ByteBuffer buffer;
        while (channelAvailableBytes(fileChannel) > 0 && !eolFound) {
            buffer = ByteBuffer.allocate(1);
            fileChannel.read(buffer);
            buffer.position(0);
            if (buffer.get() == newLineBytes[0]) {
                fileChannel.position(fileChannel.position() - 1);
                buffer = ByteBuffer.allocate(newLineBytes.length);
                fileChannel.read(buffer);
                buffer.position(0);
                if (Arrays.equals(buffer.array(), newLineBytes)) {
                    eolFound = true;
                } else {
                    fileChannel.position(fileChannel.position() - newLineBytes.length + 1);
                }
            }
        }
        long newPosition = fileChannel.position();
        int lineLength = (int) (newPosition - previousPosition);
        if (eolFound) {
            lineLength -= newLineBytes.length;
        }
        buffer = ByteBuffer.allocate(lineLength);
        fileChannel.position(previousPosition);
        fileChannel.read(buffer);
        buffer.position(0);
        fileChannel.position(newPosition);
        return buffer.array();
    }

    private static String tryNextString(FileChannel fileChannel) throws IOException {
        long previousPosition = fileChannel.position();
        String out = new String(readBytesOfLineFromChannel(fileChannel), Configuration.defaultCodepage);
        fileChannel.position(previousPosition);
        return out;
    }


    public static void printFileTypes(Map<Path, List<String>> files) {
        files.entrySet().forEach((e) -> {
            System.out.println(e.getKey().toString() + ": " + determineTypeOfTxtHistory(e.getValue()));
        });

    }

    private static txtHistoryType determineTypeOfTxtHistory(List<String> fileLines) {
        for (int i = 0; i < fileLines.size(); i++) {
            String line = fileLines.get(i);
            if (line.isEmpty()) continue;
            if (line.matches(qip_icq_separator_pattern)) {
                if (i + 1 < fileLines.size()) {
                    if (fileLines.get(i + 1).matches(qip_icq_timeline_pattern)) return txtHistoryType.QIP_ICQ;
                }
            }
            if (line.matches(mchat_line_pattern)) return txtHistoryType.MCHAT;
            if (line.matches(rnq_line_pattern)) return txtHistoryType.RNQ;
        }


        return txtHistoryType.NO_HISTORY;
    }

    private static int channelAvailableBytes(FileChannel channel) throws IOException {
        return (int) (channel.size() - channel.position());
    }

    enum txtHistoryType {
        NO_HISTORY,
        QIP_ICQ,
        MCHAT,
        RNQ
    }
}
