import java.util.HashMap;

public class ChatStatistics {

    private static int chatsOverall = 0;
    private static int messagesOverall = 0;
    private static int maxMessageLength = 0;
    private static int minMessageLength = Integer.MAX_VALUE;
    private static int avgMessageLength = 0;
    private static int encodedMessages = 0;
    private static int corruptedMessages = 0;
    public static final HashMap<String, Integer> tOMsgFieldIdMap = new HashMap<>();
    public static final HashMap<Integer, Integer> tOMsgFieldIdIntMap = new HashMap<>();
    public static final HashMap<Integer, Integer> idBlockSizeMap = new HashMap<>();
    public static final HashMap<Integer, Integer> typeOfSendingDateFieldMap = new HashMap<>();
    public static final HashMap<Integer, Integer> sendingDateFieldSizeMap = new HashMap<>();
    public static final HashMap<Integer, Integer> typeOfFieldUnknownMap = new HashMap<>();
    public static final HashMap<Integer, Integer> typeOfFieldUnknown2Map = new HashMap<>();
    public static int numberOfSentMsgs = 0;
    public static final HashMap<Message.typesOfMsgField, Integer> typeOfMessageFieldMap = new HashMap<>();
    public static final HashMap<Integer, Integer> messageLengthBlockSizeMap = new HashMap<>();


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
        tOMsgFieldIdIntMap.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("**** tOMsgFieldIdMap: ");
        tOMsgFieldIdMap.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("**** idBlockSizeMap: ");
        idBlockSizeMap.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("**** typeOfSendingDateFieldMap: ");
        typeOfSendingDateFieldMap.forEach((key, value) -> System.out.println(key + ": " + value));
        System.out.println("**** sendingDateFieldSizeMap: ");
        sendingDateFieldSizeMap.forEach((key4, value4) -> System.out.println(key4 + ": " + value4));
        System.out.println("**** typeOfFieldUnknownMap: ");
        typeOfFieldUnknownMap.forEach((key3, value3) -> System.out.println(key3 + ": " + value3));
        System.out.println("**** typeOfFieldUnknown2Map: ");
        typeOfFieldUnknown2Map.forEach((key2, value2) -> System.out.println(key2 + ": " + value2));
        System.out.println("**** messageLengthBlockSizeMap: ");
        messageLengthBlockSizeMap.forEach((key1, value1) -> System.out.println(key1 + ": " + value1));
        System.out.println("**** typeOfMessageFieldMap: ");
        typeOfMessageFieldMap.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    private static <T> void addToHasMap(HashMap<T, Integer> map, T toAdd) {
        map.computeIfPresent(toAdd, (k, v) -> v + 1);
        map.putIfAbsent(toAdd, 1);
    }
}
