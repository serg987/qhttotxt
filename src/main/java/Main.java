import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {

        String basePath = "E:\\!Temp\\210420\\History\\History";

        // combineChats(basePath);
        convertFiles(basePath);
        // pathList.stream().map(Path::getFileName).forEach(System.out::println);

        // Path::getParent - directory

        // Path::getFileName - file name
        //System.out.println(pathList.toString());

    }

    private static void convertFiles(String basePath) {
        saveFiles(getChatsFromDir(basePath));
    }

    private static void printChatStatistics(String basePath) { // TODO Service method; delete after debugging
        HashMap<Path, Chat> chatHashMap = getChatsFromDir(basePath);
        chatHashMap.entrySet().stream().map(entry -> entry.getValue()).forEach(ChatStatistics::collectStatistics);
        ChatStatistics.printStatistics();
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

    private static HashMap<Path, Chat> getChatsFromDir(String basePath) {
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

    private static void combineChats(String basePath) {
        HashMap<String, Chat> chatHashMap = new HashMap<>();
        HashMap<Path, Chat> pathChatHashMap = getChatsFromDir(basePath);
        pathChatHashMap.entrySet().forEach(entry -> {
            Chat chat = entry.getValue();
            chatHashMap.computeIfAbsent(chat.uin, ch -> chat);
            chatHashMap.computeIfPresent(chat.uin, (uinInMap, ch) -> {
                ch.messages.addAll(chat.messages);
                return ch;
            });
        });
        deleteDuplicates(chatHashMap);
        sortMessagesByTime(chatHashMap);
        saveCombinedChats(chatHashMap, basePath);
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

    private static void deleteDuplicates(HashMap<String, Chat> chatHashMap) {
        HashMap<Long, Message> messageHashMap = new HashMap<>();
        for (String uin : chatHashMap.keySet()) {
            Chat chat = chatHashMap.get(uin);
            System.out.println("Before duplicates: " + chat.messages.size());
            int i = 0;
            while (i < chat.messages.size()) {
                boolean duplicateFound = false;
                Message message = chat.messages.get(i);
                long startUnixDate = message.unixDate * 1000l;
                while (messageHashMap.containsKey(startUnixDate) && !duplicateFound) {
                    Message messageFromSet = messageHashMap.get(startUnixDate);
                    if (Arrays.equals(messageFromSet.getMessageByteArray(), message.getMessageByteArray())) {
                        duplicateFound = true;
                        chat.messages.remove(message);
                        i--;
                    }
                    startUnixDate++;
                }
                if (!duplicateFound) {
                    messageHashMap.put(startUnixDate, message);
                }
                i++;
            }
            System.out.println("After duplicates: " + chat.messages.size());
        }
    }

    private static void sortMessagesByTime(HashMap<String, Chat> chatHashMap) {
        for (String uin : chatHashMap.keySet()) {
            Chat chat = chatHashMap.get(uin);
            chat.messages = chat.messages.stream()
                    .sorted((a, b) -> (a.unixDate - b.unixDate)).collect(Collectors.toList());
        }
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

    private static void showHelp() {
        System.out.println("QIP history (*.qhf or *.ahf) converter to *.txt files. Usage:");
        System.out.println("QIP history (*.qhf or *.ahf) converter to *.txt files. Usage:");
    }

    private class SortMessagesByTime implements java.util.Comparator<Message> {
        public int compare(Message a, Message b) {
            return a.unixDate - b.unixDate;
        }
    }
}
