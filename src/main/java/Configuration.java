import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.regex.Pattern;

public class Configuration {
    public static String ownNickName;
    public static String defaultCodepage;
    public static ZoneId zoneId;
    public static String workingDir;
    public static boolean recursiveSearch;
    public static boolean combineHistories;

    // Local constants
    public static final String contactListName = "contacts.txt";

    // Patterns for parsing txt files
    public static final String qip_icq_separator = "^[-]{38}[<>][-]";
    public static final String qip_icq_timeline = "^[\\d|\\p{L}|\\s|@|\\.]*\\s[(][\\d|:]*\\s[\\d|\\/|\\.]*[)]";
    public static final String mchat_line_header = "^([\\d]{2}[\\/|\\.]){2}[\\d]{2,4}\\s[\\d]{1,2}([:][\\d]{2}){2}[<|>]";
    public static final String mchat_line = mchat_line_header + ".*";
    public static final String rnq_line = "^([\\d]{2}[\\/|\\.]){2}[\\d]{2,4}\\s" +
            "([\\d]{2}[:]){2}[\\d]{2}\\s[\\d]{1,12}\\s$";

    public static final Pattern mchatLineHeaderPattern = Pattern.compile(mchat_line_header);
    public static final Pattern rnqLineHeaderPattern = Pattern.compile(rnq_line);
    public static final Pattern qipIcqSeparatorPattern = Pattern.compile(qip_icq_separator);

    public static final String lineSeparator = System.getProperty("line.separator");
    public static final byte[] newLineBytes = lineSeparator.getBytes();

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
    public static final String analyzingFolders = "Seeking for %s files...";
    public static final String startToReadFiles = "Reading files...";
    public static final String done = "Done.";
    public static final String foundNFiles = "Found %d files.";
    public static final String combiningFiles = "Combining files...";
    public static final String deletingIdenticalMessages = "Deleting identical messages...";
    public static final String sortingMessages = "Putting all messages back together...";
    public static final String savingFiles = "Saving history...";
    public static final String moreThanOnePaths = "Sorry! Only one path (directory/file) is allowed";
    public static final String welcomeMessage = "QHF (AHF) to TXT converter. Developed by Sergey Kiselev in 2021. " +
            "https://github.com/serg987/qhttotxt";
    public static final String forHelp = "For quick help run with -h";
    public static final String onlyOneParam = "Error! Only single parameter allowed: %s";
    public static final String contact_info_in_chat_title = "[QhtToTxt] Chat between %s and %s";
    public static final String contact_info_in_chat = "[QhtToTxt] Contact info: uin: %s; known name(s): %s; " +
            "knows groups: %s";
    public static final String configMsg = "Current configuration:\n" +
            "Working path: %s\n" +
            "Go recursive: %b; combine histories: %b; your nickname: '%s'; time zone: '%s'; codepage: '%s'";
    public static final String noContactListsFound = "No contact list files found or no contacts were " +
            "found inside them";
    public static final String foundNContacts = "Found %d contacts.";
    public static final String savingContactList = "Saving contact list to '%s' - %d contacts";
    public static final String foundTxtChatWith = "Found %s chat with %s. It has %d messages.";

    public static final String helpMsg = "\n\tParameters (all are optional):\n" +
            "{path} - set the path (current path by default, only one path allowed)\n" +
            "-c - combine histories for one uin from different files (false by default); " +
            "output files will be placed in the working directory\n" +
            "-h - this help\n" +
            "-n {nickname} - set your nickname for displaying in histories ('You' by default)\n" +
            "-p {codepage} - explicitly set the codepage of qhf files (system codepage by default); " +
            "for Windows and Russian it is usually \"windows-1251\", " +
            "see other codepages https://en.wikipedia.org/wiki/Code_page\n" +
            "-r - turn on recursive search for files in subfolders (false by default)\n" +
            "-z {timezone} - explicitly set the timezone for histories " +
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
        defaultCodepage = System.getProperty("file.encoding", "UTF-8");
        zoneId = ZoneId.systemDefault();
        ownNickName = "You";
        recursiveSearch = false;
        combineHistories = false;
    }
}
