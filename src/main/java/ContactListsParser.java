import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ContactListsParser {

    private static final ContactList contactList = new ContactList();

    public static void parseContactListFiles() {
        parseCbdFiles();
        parseClFiles();
        int cLSize = ContactList.getContactList().size();
        if (cLSize == 0) {
            System.out.println(Configuration.noContactListsFound);
        } else {
            System.out.printf((Configuration.foundNContacts) + "%n", cLSize);
        }

    }

    private static void parseCbdFiles() {
        List<Path> pathList = IOHelper.getPathListCdb();
        List<List<String>> cdbFiles = new ArrayList<>(IOHelper.convertFilesToStrings(pathList)
                .values());
        for (List<String> fileLInes : cdbFiles) parseCdb(fileLInes);
    }

    private static void parseClFiles() {
        List<Path> pathList = IOHelper.getPathListCl();
        List<List<String>> cdbFiles = new ArrayList<>(IOHelper.convertFilesToStrings(pathList)
                .values());
        for (List<String> fileLines : cdbFiles) parseCl(fileLines);
    }

    private static void parseCdb(List<String> fileLInes) {
        for (String str : fileLInes) {
            String id = "";
            String name = "";
            String group = "";
            boolean isGroup = false;
            if (!str.contains("ID=")) continue;
            String[] params = str.split("\\|");
            for (String param : params) {
                if (param.startsWith("ID=")) id = param.substring(3);
                if (param.startsWith("DisplayName=")) name = param.substring(12);
                if (param.startsWith("GroupName=")) group = param.substring(10);
                if (param.equals("IsGroupRecord=True")) isGroup = true;
            }
            if (!isGroup && !id.isEmpty()) contactList.addContact(id, name, group);
        }
    }

    private static void parseCl(List<String> fileLInes) {
        for (String str : fileLInes) {
            String[] params = str.split(";");
            String id = params[1];
            String name = params[2];
            String group = params[0];
            if (!id.isEmpty()) contactList.addContact(id, name, group);
        }
    }

    public static void saveContactList(Path path) {
        File fileToSave = new File(path.toUri());
        StringBuilder stringBuilder = new StringBuilder();
        FileWriter writer = null;

        System.out.printf((Configuration.savingContactList) + "%n",
                path.toAbsolutePath().toString(),
                ContactList.getContactList().size());
        HashMap<String, ContactList.Contact> contactList = ContactList.getContactList();
        List<String> sortedUins = contactList.keySet().stream().sorted().collect(Collectors.toList());
        for (String uin : sortedUins) {
            stringBuilder.append("Uin: ").append(uin).append("; name(s): ");
            stringBuilder.append(contactList.get(uin).getNames());
            stringBuilder.append("; group(s): ");
            stringBuilder.append(contactList.get(uin).getGroups());
            Commons.addCRtoStringBuilder(stringBuilder);
        }
        try {
            writer = new FileWriter(fileToSave);
            writer.write(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
