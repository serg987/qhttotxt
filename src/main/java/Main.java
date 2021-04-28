import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        List<String> argsList = Arrays.asList(args);

        System.out.println(Configuration.welcomeMessage);

        if (argsList.isEmpty()) System.out.println(Configuration.forHelp);

        if (argsList.contains("-h") || argsList.contains("-H")) {
            System.out.println(Configuration.helpMsg);
            System.exit(0);
        }

        parseArgsAndCreateConfig(argsList);

        try {
            System.out.printf((Configuration.configMsg) + "%n", Configuration.workingDir,
                    Configuration.recursiveSearch, Configuration.combineHistories,
                    new String(Configuration.ownNickName.getBytes(StandardCharsets.UTF_8), Configuration.defaultCodepage), Configuration.zoneId, Configuration.defaultCodepage);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (Configuration.combineHistories && Files.isDirectory(Paths.get(Configuration.workingDir))) {
            ContactListsParser.parseContactListFiles();
            if (!ContactList.getContactList().isEmpty()) {
                Path contactListPath = Paths.get(Configuration.workingDir, Configuration.contactListName);
                ContactListsParser.saveContactList(contactListPath);
            }
            Combiner.combineChats();
        } else IOHelper.convertFiles();

        System.exit(0);
    }

    private static void parseArgsAndCreateConfig(List<String> argsList) {
        for (int i = 0; i < argsList.size(); i++) {
            if (argsList.get(i).isEmpty()) continue;

            if (argsList.get(i).startsWith("-") && argsList.get(i).length() > 2) {
                System.out.printf((Configuration.onlyOneParam) + "%n", argsList.get(i));
                System.exit(1);
            }

            if (argsList.get(i).startsWith("-")) {
                char param = argsList.get(i).toLowerCase().charAt(1);
                switch (param) {
                    case 'c':
                        Configuration.combineHistories = true;
                        break;
                    case 'r':
                        Configuration.recursiveSearch = true;
                        break;
                }
                if (i + 1 < argsList.size()) {
                    switch (param) {
                        case 'z':
                            Configuration.zoneId = ZoneId.of(argsList.get(i + 1));
                            i++;
                            break;
                        case 'p':
                            Configuration.defaultCodepage = argsList.get(i + 1);
                            i++;
                            break;
                        case 'n':
                            Configuration.ownNickName = Commons
                                        .createInternalJavaStringForOutsideText(argsList.get(i + 1));
                            i++;
                            break;
                    }
                }
            } else {
                if (Configuration.workingDir != null) {
                    System.out.println(Configuration.moreThanOnePaths);
                } else {
                    Configuration.workingDir = argsList.get(i);
                }
            }
        }
        if (Configuration.workingDir == null || Configuration.workingDir.isEmpty()) {
            Configuration.workingDir = System.getProperty("user.dir");
        }
    }
}
