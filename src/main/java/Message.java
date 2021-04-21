import java.io.UnsupportedEncodingException;

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
    //private String message;
    public boolean isEncoded = false;
    private byte[] messageBytes;
    private byte[] decodedBytes;

    public void setTypeOfMsgField(byte b) {
        switch (b) {
            case 02: typeOfMessageField = typesOfMsgField.MESSAGE_SENDING_DATE; break;
            case 03: typeOfMessageField = typesOfMsgField.MESSAGE_SENDER; break;
            case 05: typeOfMessageField = typesOfMsgField.AUTH_REQUEST; break;
            case 06: typeOfMessageField = typesOfMsgField.FRIEND_REQUEST; break;
            case 13: typeOfMessageField = typesOfMsgField.RECIEVED_OFFLINE; break;
            case 14: typeOfMessageField = typesOfMsgField.AUTH_RECIEVED; break;
            case 80: typeOfMessageField = typesOfMsgField.TYPE_80; break;
            case 81: typeOfMessageField = typesOfMsgField.TYPE_81; break;
            default: typeOfMessageField = typesOfMsgField.RECIEVED_ONLINE;
        }
    }

    public void setMessageByteArray(byte[] bytes) {
        messageBytes = bytes;
    }

    public byte[] getMessageByteArray() {
        if (isEncoded) return getDecodedMessageBytes();
        return messageBytes;
    }

    public String getMessage() {
        try {
            return new String(getMessageByteArray(), Configuration.defaultEncoding);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Configuration.getNoCodepageFound();
    }

    private byte[] getDecodedMessageBytes() {
        if (decodedBytes == null && messageBytes != null) {
            decodedBytes = new byte[messageBytes.length];
            for (int i = 0; i < messageBytes.length; i++)
                decodedBytes[i] = (byte) (255 - (messageBytes[i] & 0xFF) - i - 1);
        }
        return decodedBytes;
    }

    enum typesOfMsgField {
        RECIEVED_ONLINE,
        MESSAGE_SENDING_DATE,
        MESSAGE_SENDER,
        AUTH_REQUEST,
        FRIEND_REQUEST,
        RECIEVED_OFFLINE,
        AUTH_RECIEVED,
        TYPE_80,
        TYPE_81,
    }
}
