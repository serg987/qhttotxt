import java.util.ArrayList;
import java.util.List;

public class Chat {
    String header;
    int historySize;
    int numberOfMsgs;
    int numberOfMsgs2;
    int uinLength;
    String uin;
    int nickNameLength;
    String nickName;

    public List<Message> messages = new ArrayList<>();

    public String getNickNameForFileName() {
        return (uin.equals(nickName)) ? "" : "_" + nickName.replaceAll("[\\\\/:*?\"<>|]", "");
    }
}
