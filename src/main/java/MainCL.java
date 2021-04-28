import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainCL {
    public static void main1(String[] args) {
        Configuration.workingDir = "E:\\!Temp\\210423\\CL";
        Configuration.recursiveSearch = true;
        ContactListsParser.parseContactListFiles();

        File fileToSave = new File(Configuration.workingDir.concat("\\1.txt"));
        Path path = fileToSave.toPath();
        ContactListsParser.saveContactList(path);
    }

    public static void main(String[] args) {
        Configuration.workingDir = "E:\\!Temp\\210423\\TxtHistory";
        Configuration.recursiveSearch = true;

        List<Path> paths = IOHelper.getPathListTxt();
        Map<Path, List<String>> map = IOHelper.convertFilesToStrings(paths, Charset.forName(Configuration.defaultCodepage));
        Map<Path, Chat> chatMap = new HashMap<>();
        for (Path path : paths) {
            Chat chat = TxtHistoryParser.parseChatFromTxt(path, map.get(path));
            File fileToSave = new File(path.toAbsolutePath().toString().replace(".txt", "-new.txt"));
            Path newPath = fileToSave.toPath();
            chatMap.put(newPath, chat);
        }

        for (Path path : chatMap.keySet()) {
            try {
                IOHelper.saveChatToTxt(chatMap.get(path), path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
