public class Message {
    public int msgBlockSize;
    public int tOMsgFieldId;
    public int idBlockSize;
    public int id;
    public int typeOfSendingDateField;
    public int sendingDateFieldSize;
    public int unixDate;
    public int typeOfFieldUnknown;
    public int typeOfFieldUnknown2;
    public boolean isSent;
    public typesOfMsgField typeOfMessageField;
    public int messageLengthBlockSize;
    public int messageLength;
    public int corruptedBytesNum;
    public boolean isEncoded = false;
    public String messageText;

    public void setTypeOfMsgField(byte b) {
        switch (b) {
            case 2: typeOfMessageField = typesOfMsgField.MESSAGE_SENDING_DATE; break;
            case 3: typeOfMessageField = typesOfMsgField.MESSAGE_SENDER; break;
            case 5: typeOfMessageField = typesOfMsgField.AUTH_REQUEST; break;
            case 6: typeOfMessageField = typesOfMsgField.FRIEND_REQUEST; break;
            case 13: typeOfMessageField = typesOfMsgField.RECEIVED_OFFLINE; break;
            case 14: typeOfMessageField = typesOfMsgField.AUTH_RECEIVED; break;
            case 80: typeOfMessageField = typesOfMsgField.TYPE_80; break;
            case 81: typeOfMessageField = typesOfMsgField.TYPE_81; break;
            default: typeOfMessageField = typesOfMsgField.RECEIVED_ONLINE;
        }
    }

    public void setMessageByteArray(byte[] bytes) {
        byte[] messageBytes = (isEncoded) ? decodeBytes(bytes) : bytes;
        messageText = Commons.guessCodePageAndConvertIfNeeded(messageBytes);
    }

    public void addLineToMessageText(String message) {
        messageText = messageText.concat(Configuration.lineSeparator).concat(message);
    }

    private byte[] decodeBytes(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) (255 - (bytes[i] & 0xFF) - i - 1);
        return bytes;
    }

    enum typesOfMsgField {
        RECEIVED_ONLINE,
        MESSAGE_SENDING_DATE,
        MESSAGE_SENDER,
        AUTH_REQUEST,
        FRIEND_REQUEST,
        RECEIVED_OFFLINE,
        AUTH_RECEIVED,
        TYPE_80,
        TYPE_81,
    }
}
