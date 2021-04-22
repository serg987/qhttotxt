import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class IOHelper {
    public static void saveFiles(String basePath) {
        saveFiles(getChatsFromDir(basePath));
    }

    private static void saveFiles(HashMap<Path, Chat> chatHashMap) {
        chatHashMap.entrySet().forEach((entry) -> {
            Path path = entry.getKey();
            Chat chat = entry.getValue();
            String fileName = path.getFileName().toString().toLowerCase()
                    .replace(".qhf", ".txt").replace(".ahf", ".txt");
            Path outPath = Paths.get(path.getParent().toString(), fileName);
            try {
                QhfParser.saveChatToTxt(chat, outPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private static void printChatStatistics(String basePath) { // TODO Service method; delete after debugging
        HashMap<Path, Chat> chatHashMap = getChatsFromDir(basePath);
        chatHashMap.entrySet().stream().map(entry -> entry.getValue()).forEach(ChatStatistics::collectStatistics);
        ChatStatistics.printStatistics();
    }

    public static HashMap<Path, Chat> getChatsFromDir(String basePath) {
        List<Path> pathList = getPathList(basePath, true); // TODO change true to a param
        HashMap<Path, Chat> chatHashMap = new HashMap<>();
        try {
            for (Path path : pathList) {
                Chat chat = QhfParser.parseQhfFile(path);
                chatHashMap.put(path, chat);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chatHashMap;
    }

    public static void saveCombinedChats(HashMap<String, Chat> chatHashMap, String basePath) {
        chatHashMap.entrySet().forEach((entry) -> {
            String uin = entry.getKey();
            Chat chat = entry.getValue();
            Path outPath = Paths.get(basePath, uin + "_" + Configuration.ownNickName + ".txt");
            try {
                QhfParser.saveChatToTxt(chat, outPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    private static List<Path> getPathList(String basePath, boolean isRecursively) {
        List<Path> files = new ArrayList<>();
        Path filePath = Paths.get(basePath);
        if (!Files.exists(filePath)) {
            System.out.println(String.format(Configuration.noPathFound, basePath));
        } else if (Files.isRegularFile(filePath)) {
            files.add(filePath);
        } else {
            try {
                files = Files.find(Paths.get(basePath), isRecursively ? Integer.MAX_VALUE : 1,
                        ((path, basicFileAttributes) -> basicFileAttributes.isRegularFile()))
                        .filter(file -> {
                                    String fileName = file.getFileName().toString().toLowerCase();
                                    return fileName.endsWith(".qhf") || fileName.endsWith(".ahf");
                                }
                        )
                        .collect(Collectors.toList());

            } catch (IOException e) {
                System.out.println(String.format(Configuration.noFilesFound, basePath));
                e.printStackTrace();
            }
        }
        return files;
    }
}
