import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class IOHelper {
    public static void convertFiles() {
        saveFiles(getChatsFromDir());
    }

    private static void saveFiles(HashMap<Path, Chat> chatHashMap) {
        System.out.println(Configuration.savingFiles);
        chatHashMap.forEach((path, chat) -> {
            String fileName = path.getFileName().toString().toLowerCase()
                    .replace(".qhf", "").replace(".ahf", "")
                    .concat(getNickNameForFileName(chat)).concat(".txt");
            Path outPath = Paths.get(path.getParent().toString(), fileName);
            try {
                saveChatToTxt(chat, outPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println(Configuration.done);
    }

    private static void printChatStatistics() { // TODO Service method; delete after debugging
        HashMap<Path, Chat> chatHashMap = getChatsFromDir();
        chatHashMap.values().forEach(ChatStatistics::collectStatistics);
        ChatStatistics.printStatistics();
    }

    public static HashMap<Path, Chat> getChatsFromDir() {
        List<Path> pathList = getPathListQHF();
        HashMap<Path, Chat> chatHashMap = new HashMap<>();
        System.out.println(Configuration.startToReadFiles);
        try {
            for (Path path : pathList) {
                Chat chat = QhfParser.parseQhfFile(path);
                chatHashMap.put(path, chat);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Configuration.done);
        return chatHashMap;
    }

    public static Map<Path, List<String>> convertFilesToStrings(List<Path> pathList, Charset charset) {
        Map<Path, List<String>> fileLinesMap = new HashMap<>();
        for (Path path : pathList) {
            fileLinesMap.put(path, convertFileToStrings(path, charset));
        }
        System.out.println(Configuration.done);
        return fileLinesMap;
    }

    public static List<String> convertFileToStrings(Path path, Charset charset) {
        List<String> fileLines = null;
        try {
            File file = new File(path.toUri());
            FileInputStream fs = new FileInputStream(file);
            BufferedReader in = new BufferedReader(new InputStreamReader(fs, charset));
            fileLines = in.lines().collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileLines;
    }

    public static void saveCombinedChats(HashMap<String, Chat> chatHashMap) {
        System.out.println(Configuration.savingFiles);
        chatHashMap.forEach((uin, chat) -> {
            Path outPath = Paths.get(Configuration.workingDir, uin
                    + getNickNameForFileName(chat) + "_" + Configuration.ownNickName + ".txt");
            try {
                saveChatToTxt(chat, outPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println(Configuration.done);
    }


    private static List<Path> getPathList(String[] extensions) {
        List<Path> files = new ArrayList<>();
        System.out.println(
                String.format(Configuration.analyzingFolders,
                        Commons.populateStringWithListElems(
                                Arrays.asList(extensions)).toString()));
        Path filePath = Paths.get(Configuration.workingDir);
        if (!Files.exists(filePath)) {
            System.out.printf((Configuration.noPathFound) + "%n", Configuration.workingDir);
        } else if (Files.isRegularFile(filePath)) {
            files.add(filePath);
        } else {
            try {
                files = Files.find(Paths.get(Configuration.workingDir),
                        Configuration.recursiveSearch ? Integer.MAX_VALUE : 1,
                        ((path, basicFileAttributes) -> basicFileAttributes.isRegularFile()))
                        .filter(file -> {
                                    String fileName = file.getFileName().toString().toLowerCase();
                                    return Arrays.stream(extensions).anyMatch(s -> fileName.endsWith(s));
                                }
                        )
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (files.isEmpty()) {
            System.out.printf((Configuration.noFilesFound) + "%n", Configuration.workingDir);
        } else {
            System.out.printf((Configuration.foundNFiles) + "%n", files.size());
        }
        return files;
    }

    public static List<Path> getPathListTxt() {
        String[] extensions = {".txt"};
        return getPathList(extensions);
    }

    public static List<Path> getPathListCl() {
        String[] extensions = {".cl"};
        return getPathList(extensions);
    }

    public static List<Path> getPathListCdb() {
        String[] extensions = {".cdb"};
        return getPathList(extensions);
    }

    private static List<Path> getPathListQHF() {
        String[] extensions = {".qhf", ".ahf"};
        return getPathList(extensions);
    }

    private static String getNickNameForFileName(Chat chat) {
        //String nickName = "";
       // try {
            String nickName = (chat.uin.equals(chat.nickName)) ? "" : "_" + chat.nickName
       //             new String(chat.nickName.getBytes(Configuration.defaultCodepage), StandardCharsets.UTF_8)
                            .replaceAll("[\\\\/:*?\"<>|]", "");
       // } catch (UnsupportedEncodingException e) {
       //     e.printStackTrace();
       // }
        return nickName;
    }

    public static void saveChatToTxt(Chat chat, Path path) throws IOException {
        File fileToSave = new File(path.toUri());

        try (FileOutputStream outputStream = new FileOutputStream(fileToSave)) {
            StringBuilder stringBuilder = new StringBuilder();

            if (Configuration.combineHistories) ContactList.addContactInfoToStrBuilder(stringBuilder, chat);

            for (Message m : chat.messages) {
                ZonedDateTime zonedDateTime = Instant.ofEpochSecond(m.unixDate).atZone(Configuration.zoneId);
                stringBuilder.append("--------------------------------------")
                        .append(m.isSent ? ">" : "<").append("-");
                Commons.addCRtoStringBuilder(stringBuilder);
                if (m.corruptedBytesNum > 0) {
                    stringBuilder.append(String.format(Configuration.messageIsCorrupted, m.corruptedBytesNum));
                    Commons.addCRtoStringBuilder(stringBuilder);
                }
                outputStream.write(stringBuilder.toString().getBytes(Configuration.defaultCodepage));
                stringBuilder.setLength(0);
                String nickName = (m.isSent) ? Configuration.ownNickName : chat.nickName;
                outputStream.write(nickName.getBytes(StandardCharsets.UTF_8));
                stringBuilder.append(" (")
                        .append(zonedDateTime.format(DateTimeFormatter.ofPattern(Configuration.timePatternInTxt)))
                        .append(")");
                Commons.addCRtoStringBuilder(stringBuilder);
                outputStream.write(stringBuilder.toString().getBytes(Configuration.defaultCodepage));
                stringBuilder.setLength(0);
                String utf8 = new String(m.getMessageByteArray(), StandardCharsets.UTF_8);
                String cp1251 = new String(m.getMessageByteArray(), Configuration.defaultCodepage);
                String converted = guessCodePageAndConvertIfNeeded(m.getMessageByteArray());
                // outputStream.write(m.getMessageByteArray());
                stringBuilder.append(converted);
                Commons.addCRtoStringBuilder(stringBuilder);
                Commons.addCRtoStringBuilder(stringBuilder);
                outputStream.write(stringBuilder.toString().getBytes(Configuration.defaultCodepage));
                stringBuilder.setLength(0);
            }

            outputStream.flush();
        } catch (UnsupportedEncodingException e) {
            System.out.println(Configuration.getNoCodepageFound());
            e.printStackTrace();
        }
    }

    private static String guessCodePageAndConvertIfNeeded(byte[] bytes) {
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        Charset codepage = Charset.forName(Configuration.defaultCodepage);
        char[] utf8chars = utf8.toCharArray();
        int i = 0;
        boolean isUtf = true;
        while (i < utf8chars.length && isUtf) {
            if (utf8chars[i] > 65500) isUtf = false;
            i++;
        }
        if (isUtf) codepage = StandardCharsets.UTF_8;
        return new String(bytes, codepage);
    }
}
