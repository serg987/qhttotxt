import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ContactListsParser {

    private static ContactList contactList = new ContactList();

    public static void parseContactListFiles() {
        parseCbdFiles();
        parseClFiles();
        System.out.println("Found contacts: " + ContactList.getContactList().size());
    }

    private static void parseCbdFiles() {
        List<Path> pathList = IOHelper.getPathListCdb();
        List<String> cdbFiles = IOHelper.convertFilesToStrings(pathList, StandardCharsets.UTF_16);
        for (String file : cdbFiles) parseCdb(file);
    }

    private static void parseClFiles() {
        List<Path> pathList = IOHelper.getPathListCl();
        List<String> cdbFiles = IOHelper.convertFilesToStrings(pathList, Charset.forName(Configuration.defaultCodepage));
        for (String file : cdbFiles) parseCl(file);
    }

    private static void parseCdb(String cdbFile) {
        String[] lines = cdbFile.split(System.getProperty("line.separator"));

        for (String str : lines) {
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

    private static void parseCl(String clFile) {
        String[] lines = clFile.split(System.getProperty("line.separator"));

        for (String str : lines) {
            String[] params = str.split(";");
            String id = params[1];
            String name = params[2];
            String group = params[0];
            if (!id.isEmpty()) contactList.addContact(id, name, group);
        }
    }

    public static void saveContactList(Path path) {
        File fileToSave = new File(path.toUri());

        HashMap<String, ContactList.Contact> contactList = ContactList.getContactList();
        List<String> sortedUins = contactList.keySet().stream().sorted().collect(Collectors.toList());
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(fileToSave);

            for (String uin : sortedUins) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Uin: ").append(uin).append("; name(s): ");
                stringBuilder.append(Commons.populateStringWithListElems(contactList.get(uin).displayNames));
                stringBuilder.append("; group(s): ");
                stringBuilder.append(Commons.populateStringWithListElems(contactList.get(uin).groups));
                Commons.addCRtoStringBuilder(stringBuilder);
                outputStream.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
            }
            outputStream.flush();
        } catch (UnsupportedEncodingException e) {
            System.out.println(Configuration.getNoCodepageFound());
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
