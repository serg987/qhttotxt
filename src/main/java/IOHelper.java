import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class IOHelper {
    public static void convertFiles() {
        saveFiles(getChatsFromDir());
    }

    private static void saveFiles(HashMap<Path, Chat> chatHashMap) {
        System.out.println(Configuration.savingFiles);
        chatHashMap.forEach((path, chat) -> {
            String fileName = path.getFileName().toString().toLowerCase()
                    .replace(".qhf", ".txt").replace(".ahf", ".txt");
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
        chatHashMap.values().stream().forEach(ChatStatistics::collectStatistics);
        ChatStatistics.printStatistics();
    }

    public static HashMap<Path, Chat> getChatsFromDir() {
        List<Path> pathList = getPathList();
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

    public static void saveCombinedChats(HashMap<String, Chat> chatHashMap) {
        System.out.println(Configuration.savingFiles);
        chatHashMap.forEach((uin, chat) -> {
            Path outPath = Paths.get(Configuration.workingDir, uin + "_" + Configuration.ownNickName + ".txt");
            try {
                QhfParser.saveChatToTxt(chat, outPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println(Configuration.done);
    }


    private static List<Path> getPathList() {
        List<Path> files = new ArrayList<>();
        System.out.println(Configuration.analyzingFolders);
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
                                    return fileName.endsWith(".qhf") || fileName.endsWith(".ahf");
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
}
