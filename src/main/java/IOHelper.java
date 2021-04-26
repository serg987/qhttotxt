import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                QhfParser.saveChatToTxt(chat, outPath);
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
        try {
            for (Path path : pathList) {
                File file = new File(path.toUri());
                FileInputStream fs = new FileInputStream(file);
                BufferedReader in = new BufferedReader(new InputStreamReader(fs, charset));
                List<String> fileLines = in.lines().collect(Collectors.toList());
                fileLinesMap.put(path, fileLines);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Configuration.done);
        return fileLinesMap;
    }

    public static void saveCombinedChats(HashMap<String, Chat> chatHashMap) {
        System.out.println(Configuration.savingFiles);
        chatHashMap.forEach((uin, chat) -> {
            Path outPath = Paths.get(Configuration.workingDir, uin
                    + getNickNameForFileName(chat) + "_" + Configuration.ownNickName + ".txt");
            try {
                QhfParser.saveChatToTxt(chat, outPath);
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
        String nickName = "";
        try {
            nickName = (chat.uin.equals(chat.nickName)) ? "" : "_" +
                    new String(chat.nickName.getBytes(Configuration.defaultCodepage), StandardCharsets.UTF_8)
                            .replaceAll("[\\\\/:*?\"<>|]", "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return nickName;
    }
}
