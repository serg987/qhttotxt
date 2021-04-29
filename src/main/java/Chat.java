import java.util.ArrayList;
import java.util.List;

public class Chat {
    String header;
    int historySize;
    int numberOfMsgs; // these fields are used only in QHF Parser.
    int numberOfMsgs2; // In other places messages.size() are used
    int uinLength;
    String uin;
    int nickNameLength;
    String nickName;

    public List<Message> messages = new ArrayList<>();

    public String getNickNameForFileName() {
        return (uin.equals(nickName)) ? "" : "_" + nickName.replaceAll("[\\\\/:*?\"<>|]", "");
    }
}
