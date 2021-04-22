import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Combiner {
    public static void combineChats() {
        HashMap<String, Chat> chatHashMap = new HashMap<>();
        HashMap<Path, Chat> pathChatHashMap = IOHelper.getChatsFromDir();
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
        IOHelper.saveCombinedChats(chatHashMap);
    }

    private static void deleteDuplicates(HashMap<String, Chat> chatHashMap) {
        HashMap<Long, Message> messageHashMap = new HashMap<>();
        for (String uin : chatHashMap.keySet()) {
            Chat chat = chatHashMap.get(uin);
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
        }
    }

    private static void sortMessagesByTime(HashMap<String, Chat> chatHashMap) {
        for (String uin : chatHashMap.keySet()) {
            Chat chat = chatHashMap.get(uin);
            chat.messages = chat.messages.stream()
                    .sorted((a, b) -> (a.unixDate - b.unixDate)).collect(Collectors.toList());
        }
    }
}
