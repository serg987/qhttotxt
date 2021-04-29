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
    private static final byte[] lineSeparatorBytes = Configuration.newLineBytes;
    private static final int lineSeparatorLength = lineSeparatorBytes.length;

    public static void convertFiles() {
        saveFiles(getChatsFromDir());
    }

    private static void saveFiles(HashMap<Path, Chat> chatHashMap) {
        System.out.println(Configuration.savingFiles);
        chatHashMap.forEach((path, chat) -> {
            String fileName = concatWithContactNickNameOwnNickNameTxt(path.getFileName().toString().toLowerCase()
                    .replace(".qhf", "").replace(".ahf", ""),
                    chat);
            Path outPath = Paths.get(path.getParent().toString(), fileName);
                saveChatToTxt(chat, outPath);
        });
        System.out.println(Configuration.done);
    }

    private static String concatWithContactNickNameOwnNickNameTxt(String str, Chat chat) {
        return str.concat(chat.getNickNameForFileName()).concat("_with_").concat(Configuration.ownNickName)
                .concat(".txt");
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

    public static Map<Path, List<String>> convertFilesToStrings(List<Path> pathList) {
        Map<Path, List<String>> fileLinesMap = new HashMap<>();
        for (Path path : pathList) {
            fileLinesMap.put(path, convertFileToStrings(path));
        }
        System.out.println(Configuration.done);
        return fileLinesMap;
    }

    public static List<String> convertFileToStrings(Path path) {
        byte[] fileBytes = readFileToByteArray(path);
        List<String> fileLines = new ArrayList<>();
        byte[] partForCheckingIfUtf16 = Arrays.copyOfRange(fileBytes,
                0,
                Math.min(fileBytes.length, 100));
        Charset charset = Commons.guessCharset(partForCheckingIfUtf16);
        if (charset.equals(StandardCharsets.UTF_16)) {
            InputStream stream = new ByteArrayInputStream(fileBytes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_16));
            fileLines = reader.lines().collect(Collectors.toList());
        } else {
            int i = 0;
            while (i < fileBytes.length) {
                byte[] nextLineBytes = getNextLineFromFileArray(fileBytes, i);
                String nextLine = Commons.guessCodePageAndConvertIfNeeded(nextLineBytes);
                fileLines.add(nextLine);
                i += nextLineBytes.length + lineSeparatorLength;
            }
        }

        return fileLines;
    }

    public static void saveCombinedChats(HashMap<String, Chat> chatHashMap) {
        System.out.println(Configuration.savingFiles);
        chatHashMap.forEach((uin, chat) -> {
            Path outPath = Paths.get(Configuration.workingDir,
                    concatWithContactNickNameOwnNickNameTxt(uin, chat));
                saveChatToTxt(chat, outPath);
        });

        System.out.println(Configuration.done);
    }


    private static List<Path> getPathList(String[] extensions) {
        List<Path> files = new ArrayList<>();
        System.out.printf((Configuration.analyzingFolders) + "%n",
                Commons.populateStringWithListElems(
                        Arrays.asList(extensions)).toString());
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
                                    return Arrays.stream(extensions).anyMatch(fileName::endsWith);
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

    public static void saveChatToTxt(Chat chat, Path path) {
        File fileToSave = new File(path.toUri());
        FileWriter writer = null;
        StringBuilder stringBuilder = new StringBuilder();
        if (Configuration.combineHistories) ContactList.addContactInfoToStrBuilder(stringBuilder, chat);

        for (Message m : chat.messages) {
            ZonedDateTime zonedDateTime = Instant.ofEpochSecond(m.unixDate).atZone(Configuration.zoneId);
            String nickName = (m.isSent) ? Configuration.ownNickName : chat.nickName;
            stringBuilder.append("--------------------------------------")
                    .append(m.isSent ? ">" : "<").append("-");
            Commons.addCRtoStringBuilder(stringBuilder);
            if (m.corruptedBytesNum > 0) {
                stringBuilder.append(String.format(Configuration.messageIsCorrupted, m.corruptedBytesNum));
                Commons.addCRtoStringBuilder(stringBuilder);
            }
            stringBuilder.append(nickName).append(" (")
                    .append(zonedDateTime.format(DateTimeFormatter.ofPattern(Configuration.timePatternInTxt)))
                    .append(")");
            Commons.addCRtoStringBuilder(stringBuilder);
            stringBuilder.append(m.messageText);
            Commons.addCRtoStringBuilder(stringBuilder);
            Commons.addCRtoStringBuilder(stringBuilder);
        }

        try {
            writer = new FileWriter(fileToSave);
            writer.write(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }  finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static byte[] readFileToByteArray(Path path) {
        byte[] bytes = new byte[0];
        File file = new File(path.toUri());
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(file);
            bytes = new byte[fs.available()];
            fs.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bytes;
    }

    private static byte[] getNextLineFromFileArray(byte[] bytes, int startIndex) {
        boolean eolFound = false;
        int length = bytes.length;
        int i = startIndex;
        while (i < length && !eolFound) {
            if (bytes[i] == lineSeparatorBytes[0]) {
                int j = i;
                while (j < length && j - i < lineSeparatorLength && bytes[j] == lineSeparatorBytes[j - i]) {
                    j++;
                }
                if (j - i == lineSeparatorLength) {
                    eolFound = true;
                }
            }
            i++;
        }
        return Arrays.copyOfRange(bytes, startIndex, i - 1);
    }
}
