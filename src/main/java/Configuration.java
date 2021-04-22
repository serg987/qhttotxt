import java.time.ZoneId;

public class Configuration {
    public static String ownNickName;
    public static String defaultEncoding;
    public static ZoneId zoneId;
    public static String workingDir;
    public static boolean recursiveSearch;
    public static boolean combineHistories;

    // Local service messages
    private static final String noCodepageFound = "Codepage %s cannot be applied to the text.";
    public static final String noBytesAvailable = "Unexpected end of file %s. File may be damaged.";
    public static final String notQhfFile = "The file %s is not a qip history file.";
    public static final String cannotReadMsg = "File %s is corrupted! Cannot read message.";
    public static final String timePatternInTxt = "HH:mm:ss dd/MM/yyyy";
    public static final String noFilesFound = "No files were found in provided path %s";
    public static final String messageWithZeroLength = "[QhtToTxt] No message available - message length is zero.";
    public static final String tryingToSaveCorruptedFile = "Seems like the file %s is damaged. " +
            "Trying to save %d out of %d messages.";
    public static final String messageIsCorrupted = "[QhtToTxt] Probably this message is corrupted. " +
            "There are %d bytes lost.";
    public static final String noPathFound = "The file or path %s does not exist.";
    public static final String analyzingFolders = "Seeking for .qhf or .ahf files...";
    public static final String startToReadFiles = "Reading files...";
    public static final String done = "Done.";
    public static final String foundNFiles = "Found %d files.";
    public static final String combiningFiles = "Combining files...";
    public static final String deletingIdenticalMessages = "Deleting identical messages...";
    public static final String sortingMessages = "Putting all messages back together...";
    public static final String savingFiles = "Saving history...";
    public static final String moreThanOnePaths = "Sorry! Only one path (directory/file) is allowed";
    public static final String welcomeMessage = "QHF (AHF) to TXT converter. Developed by Sergey Kiselev in 2021. https://gi"; // TODO add repo
    public static final String forHelp = "For quick help run with -h";
    public static final String onlyOneParam = "Error! Only single parameter allowed: %s";
    public static final String configMsg = "Current configuration:\n" +
            "Working path: %s\n" +
            "Go recursive: %b; combine histories: %b; your nickname: '%s'; time zone: '%s'; codepage: '%s'";

    public static final String helpMsg = "\n\tParameters (all are optional):\n" +
            "{path} - set the path (current path by default, only one path allowed)\n" +
            "-c - combine histories for one uin from different files (false by default); " +
            "output files will be placed in the working directory\n" +
            "-n - set your nickname for displaying in histories ('You' by default)\n" +
            "-p - explicitly set the codepage of qhf files (system codepage by default); " +
            "for Windows and Russian it is usually \"windows-1251\", " +
            "see other codepages https://en.wikipedia.org/wiki/Code_page\n" +
            "-r - turn on recursive search for files in subfolders (false by default)\n" +
            "-z - explicitly set the timezone for histories " +
            "(current timezone by default - if you now in a different timezone than histories were made, " +
            "you will have the wrong time in output files); " +
            "see timezones https://en.wikipedia.org/wiki/List_of_tz_database_time_zones\n" +
            "Examples of usage:\n" +
            "Convert files in a working directory:\n" +
            "\t> java -jar qhttotxt.jar\n" +
            "Convert only one file:\n" +
            "\t> java -jar qhttotxt.jar C:\\Users\\User\\Documents\\file.qhf\n" +
            "Convert files recursively in the folder with your nickname, and codepage:\n" +
            "\t> java -jar qhttotxt.jar -r -n \"John Snow\" -p \"windows-1251\"" +
            " C:\\Users\\User\\Documents\n" +
            "Convert and combine histories from all files in the folder recursively, \n" +
            "with codepage, nickname and timezone\n" +
            "\t> java -jar qhttotxt.jar -c -r -n \"John Snow\" -p \"windows-1251\" -z" +
            " \"Europe/Moscow\" C:\\Users\\User\\Documents\n";


    static {
        defaultEncoding = System.getProperty("file.encoding", "UTF-8");
        zoneId = ZoneId.systemDefault();
        ownNickName = "You";
        recursiveSearch = false;
        combineHistories = false;
    }

    public static String getNoCodepageFound() {
        return String.format(Configuration.noCodepageFound, Configuration.defaultEncoding);
    }

}
