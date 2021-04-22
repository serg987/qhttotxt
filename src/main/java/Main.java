import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        List<String> argsList = Arrays.asList(args);

        System.out.println("QHF (AHF) to TXT converter. Developed by Sergey Kiselev in 2021. https://gi"); // TODO add repository link

        if (argsList.isEmpty()) System.out.println("For quick help run with -h");

        if (argsList.contains("-h") || argsList.contains("-H")) {
            showHelp();
            System.exit(0);
        }

        parseArgsAndCreateConfig(argsList);

        printCurrentConfig();

        if (Configuration.combineHistories && Files.isDirectory(Paths.get(Configuration.workingDir))) {
            Combiner.combineChats();
        } else IOHelper.convertFiles();

        System.exit(0);
    }

    private static void parseArgsAndCreateConfig(List<String> argsList) {
        for (int i = 0; i < argsList.size(); i++) {
            if (argsList.get(i).startsWith("-") && argsList.get(i).length() > 2) {
                System.out.println("Error! Only single parameter allowed: " + argsList.get(i));
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

    private static void printCurrentConfig() {
        System.out.println("Current configuration:");
        System.out.println("Working path: " + Configuration.workingDir);
        System.out.println("Go recursive: " + Configuration.recursiveSearch +
                "; combine histories: " + Configuration.combineHistories +
                "; your nickname for histories: '" + Configuration.ownNickName +
                "'; time zone for histories: '" + Configuration.zoneId +
                "'; codepage for converting: '" + Configuration.defaultEncoding + "'");
    }

    private static void showHelp() {
        System.out.println();
        System.out.println("\tParameters (all are optional):");
        System.out.println("{path} - set the path (current path by default)");
        System.out.println("-c - combine histories for one uin from different files (false by default); " +
                "output files will be placed in the working directory");
        System.out.println("-n - set your nickname for displaying in histories ('You' by default)");
        System.out.println("-p - explicitly set the codepage of qhf files (system codepage by default); " +
                "for Windows and Russian it is usually \"windows-1251\", " +
                "see other codepages https://en.wikipedia.org/wiki/Code_page");
        System.out.println("-r - turn on recursive search for files in subfolders (false by default)");
        System.out.println("-z - explicitly set the timezone for histories " +
                "(current timezone by default - if you now in a different timezone than histories were made, " +
                "you will have the wrong time in output files); " +
                "see timezones https://en.wikipedia.org/wiki/List_of_tz_database_time_zones");
        System.out.println("Examples of usage:");
        System.out.println("Convert files in a working directory:");
        System.out.println("\t> java -jar qhttotxt.jar");
        System.out.println("Convert only one file:");
        System.out.println("\t> java -jar qhttotxt.jar C:\\Users\\User\\Documents\\file.qhf");
        System.out.println("Convert files recursively in the folder with your nickname, and codepage:");
        System.out.println("\t> java -jar qhttotxt.jar -r -n \"John Snow\" -p \"windows-1251\"" +
                " C:\\Users\\User\\Documents");
        System.out.println("Convert and combine histories from all files in the folder recursively, " +
                "with codepage, nickname and timezone");
        System.out.println("\t> java -jar qhttotxt.jar -c -r -n \"John Snow\" -p \"windows-1251\" -z" +
                " \"Europe/Moscow\" C:\\Users\\User\\Documents");

    }

    private static boolean isPath(String path) {
        File file = new File(path);
        return (file.isDirectory() || file.isFile()) && file.exists();
    }
}
