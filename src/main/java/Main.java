import java.nio.file.Files;
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

        System.out.println(String.format(Configuration.configMsg, Configuration.workingDir,
                Configuration.recursiveSearch, Configuration.combineHistories,
                Configuration.ownNickName, Configuration.zoneId, Configuration.defaultEncoding));

        if (Configuration.combineHistories && Files.isDirectory(Paths.get(Configuration.workingDir))) {
            Combiner.combineChats();
        } else IOHelper.convertFiles();

        System.exit(0);
    }

    private static void parseArgsAndCreateConfig(List<String> argsList) {
        for (int i = 0; i < argsList.size(); i++) {
            if (argsList.get(i).startsWith("-") && argsList.get(i).length() > 2) {
                System.out.println(String.format(Configuration.onlyOneParam, argsList.get(i)));
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
                            Configuration.defaultEncoding = argsList.get(i + 1);
                            i++;
                            break;
                        case 'n':
                            Configuration.ownNickName = argsList.get(i + 1);
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
