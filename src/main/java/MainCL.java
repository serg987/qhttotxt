import java.io.*;
import java.nio.file.Path;


public class MainCL {
    public static void main(String[] args) {
        Configuration.workingDir = "E:\\!Temp\\210423\\CL";
        Configuration.recursiveSearch = true;
        ContactListsParser.parseContactListFiles();

        File fileToSave = new File(Configuration.workingDir.concat("\\1.txt"));
        Path path = fileToSave.toPath();
        ContactListsParser.saveContactList(path);
    }

}
