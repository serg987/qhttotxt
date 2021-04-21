import java.nio.charset.Charset;
import java.util.HashMap;

public class ChatStatistics {

    private static int chatsOverall = 0;
    private static int messagesOverall = 0;
    private static int maxMessageLength = 0;
    private static int minMessageLength = Integer.MAX_VALUE;
    private static int avgMessageLength = 0;
    private static int encodedMessages = 0;
    private static int corruptedMessages = 0;
    public static HashMap<String, Integer> tOMsgFieldIdMap = new HashMap<>();
    public static HashMap<Integer, Integer> tOMsgFieldIdIntMap = new HashMap<>();
    public static HashMap<Integer, Integer> idBlockSizeMap = new HashMap<>();
    public static HashMap<Integer, Integer> typeOfSendingDateFieldMap = new HashMap<>();
    public static HashMap<Integer, Integer> sendingDateFieldSizeMap = new HashMap<>();
    public static HashMap<Integer, Integer> typeOfFieldUnknownMap = new HashMap<>();
    public static HashMap<Integer, Integer> typeOfFieldUnknown2Map = new HashMap<>();
    public static int numberOfSentMsgs = 0;
    public static HashMap<Message.typesOfMsgField, Integer> typeOfMessageFieldMap = new HashMap<>();
    public static HashMap<Integer, Integer> messageLengthBlockSizeMap = new HashMap<>();


    public static void collectStatistics(Chat chat) {
        chatsOverall++;
        messagesOverall += chat.messages.size();

        for (Message message : chat.messages) {
            if (message.messageLength > maxMessageLength) maxMessageLength = message.messageLength;
            if (message.messageLength < minMessageLength) minMessageLength = message.messageLength;
            if (avgMessageLength == 0) {
                avgMessageLength = message.messageLength;
            } else {
                avgMessageLength = (avgMessageLength + message.messageLength) / 2;
            }
            if (message.isEncoded) encodedMessages++;
            if (message.isSent) numberOfSentMsgs++;
            if (message.corruptedBytesNum > 0) corruptedMessages++;
            addToHasMap(tOMsgFieldIdIntMap, message.tOMsgFieldId);
            addToHasMap(idBlockSizeMap, message.idBlockSize);
            addToHasMap(typeOfSendingDateFieldMap, message.typeOfSendingDateField);
            addToHasMap(sendingDateFieldSizeMap, message.sendingDateFieldSize);
            addToHasMap(typeOfFieldUnknownMap, message.typeOfFieldUnknown);
            addToHasMap(typeOfFieldUnknown2Map, message.typeOfFieldUnknown2);
            addToHasMap(typeOfMessageFieldMap, message.typeOfMessageField);
            addToHasMap(messageLengthBlockSizeMap, message.messageLengthBlockSize);
        }

    }

    public static void printStatistics() {
        System.out.println("******* Messages statistics: *******");
        System.out.println("chatsOverall: " + chatsOverall);
        System.out.println("messagesOverall: " + messagesOverall);
        System.out.println("maxMessageLength: " + maxMessageLength);
        System.out.println("minMessageLength: " + minMessageLength);
        System.out.println("avgMessageLength: " + avgMessageLength);
        System.out.println("encodedMessages: " + encodedMessages);
        System.out.println("corruptedMessages: " + corruptedMessages);
        System.out.println("numberOfSentMsgs: " + numberOfSentMsgs);
        System.out.println("**** tOMsgFieldIdIntMap: ");
        tOMsgFieldIdIntMap.entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        System.out.println("**** tOMsgFieldIdMap: ");
        tOMsgFieldIdMap.entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        System.out.println("**** idBlockSizeMap: ");
        idBlockSizeMap.entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        System.out.println("**** typeOfSendingDateFieldMap: ");
        typeOfSendingDateFieldMap.entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        System.out.println("**** sendingDateFieldSizeMap: ");
        sendingDateFieldSizeMap.entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        System.out.println("**** typeOfFieldUnknownMap: ");
        typeOfFieldUnknownMap.entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        System.out.println("**** typeOfFieldUnknown2Map: ");
        typeOfFieldUnknown2Map.entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        System.out.println("**** messageLengthBlockSizeMap: ");
        messageLengthBlockSizeMap.entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        System.out.println("**** typeOfMessageFieldMap: ");
        typeOfMessageFieldMap.entrySet().forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
    }

    private static <T> void addToHasMap(HashMap<T, Integer> map, T toAdd) {
        map.computeIfAbsent(toAdd, k-> 1);
        map.computeIfPresent(toAdd, (k, v) -> v + 1);
    }
}
