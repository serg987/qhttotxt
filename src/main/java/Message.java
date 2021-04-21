import java.io.UnsupportedEncodingException;

public class Message {
    public int msgBlockSize;
    public typesOfMsgFieldId tOMsgFieldId;
    public int idBlockSize;
    public int id;
    public int typeOfSendingDateField;
    public int sendingDateFieldSize;
    public int unixDate;
    public int typeOfFieldUnknown;
    public int typeOfFieldUnknown2;
    public boolean isSent;
    public int typeOfMessageField;
    public int messageLengthBlockSize;
    public int messageLength;
    public int corruptedBytesNum;
    //private String message;
    public boolean isEncoded = false;
    private byte[] messageBytes;
    private byte[] decodedBytes;

    public void setTOMsgFieldId(byte b) {
        switch (b) {
            case 02: tOMsgFieldId = typesOfMsgFieldId.MESSAGE_SENDING_DATE; break;
            case 03: tOMsgFieldId = typesOfMsgFieldId.MESSAGE_SENDER; break;
            case 05: tOMsgFieldId = typesOfMsgFieldId.AUTH_REQUEST; break;
            case 06: tOMsgFieldId = typesOfMsgFieldId.FRIEND_REQUEST; break;
            case 13: tOMsgFieldId = typesOfMsgFieldId.RECIEVED_OFFLINE; break;
            case 14: tOMsgFieldId = typesOfMsgFieldId.AUTH_RECIEVED; break;
            default: tOMsgFieldId = typesOfMsgFieldId.RECIEVED_ONLINE;
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

    enum typesOfMsgFieldId {
        RECIEVED_ONLINE,
        MESSAGE_SENDING_DATE,
        MESSAGE_SENDER,
        AUTH_REQUEST,
        FRIEND_REQUEST,
        RECIEVED_OFFLINE,
        AUTH_RECIEVED
    }
}
