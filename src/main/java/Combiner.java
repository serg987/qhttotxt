import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Combiner {
    public static void combineChats() {
        System.out.println(Configuration.combiningFiles);
        HashMap<String, Chat> chatHashMap = new HashMap<>();
        HashMap<Path, Chat> pathChatHashMap = IOHelper.getChatsFromDir();
        // Experimental: combine with existing txt history
        pathChatHashMap.putAll(TxtHistoryParser.parseChatsFromTxt());

        pathChatHashMap.forEach((uin, chat) -> {
            chatHashMap.putIfAbsent(chat.uin, chat);
            chatHashMap.computeIfPresent(chat.uin, (uinInMap, ch) -> {
                if (ch.nickName.equals(ch.uin)) ch.nickName = chat.nickName;
                ch.messages.addAll(chat.messages);
                return ch;
            });
        });

        pathChatHashMap.values().forEach(ContactList::populateChatWithName);

        System.out.println(Configuration.done);
        deleteDuplicates(chatHashMap);
        sortMessagesByTime(chatHashMap);
        IOHelper.saveCombinedChats(chatHashMap);
    }

    private static void deleteDuplicates(HashMap<String, Chat> chatHashMap) {
        HashMap<Long, Message> messageHashMap = new HashMap<>();
        System.out.println(Configuration.deletingIdenticalMessages);
        for (String uin : chatHashMap.keySet()) {
            Chat chat = chatHashMap.get(uin);
            int i = 0;
            while (i < chat.messages.size()) {
                boolean duplicateFound = false;
                Message message = chat.messages.get(i);
                long startUnixDate = message.unixDate * 1000L;
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
        System.out.println(Configuration.done);
    }

    private static void sortMessagesByTime(HashMap<String, Chat> chatHashMap) {
        System.out.println(Configuration.sortingMessages);
        for (String uin : chatHashMap.keySet()) {
            Chat chat = chatHashMap.get(uin);
            chat.messages = chat.messages.stream()
                    .sorted(Comparator.comparingInt(a -> a.unixDate)).collect(Collectors.toList());
        }
        System.out.println(Configuration.done);
    }
}
