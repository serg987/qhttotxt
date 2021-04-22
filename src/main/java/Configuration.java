import java.time.ZoneId;

public class Configuration {
    public static String ownNickName;
    public static String defaultEncoding;
    public static ZoneId zoneId;
    public static String workingDir;
    public static boolean recursiveSearch;
    public static boolean combineHistories;
    public static String[] searchExtensions;

    // Local service messages
    private static final String noCodepageFound = "Codepage %s cannot be applied to the text.";
    public static final String noBytesAvailable = "Unexpected end of file %s. File may be damaged.";
    public static final String notQhfFile = "The file %s is not a qip history file.";
    public static final String cannotReadMsg = "File %s is corrupted! Cannot read message.";
    public static final String timePatternInTxt = "HH:mm:ss dd/MM/yyyy";
    public static final String noFilesFound = "No files were found in provided path %s.";
    public static final String messageWithZeroLength = "[qhtToTxt] No message available - message length is zero.";
    public static final String tryingToSaveCorruptedFile = "Seems like the file %s is damaged. " +
            "Trying to save %d out of %d messages.";
    public static final String messageIsCorrupted = "[qhtToTxt] Probably this message is corrupted. " +
            "There are %d bytes lost.";
    public static final String noPathFound = "The file or path %s does not exist.";

    static {
        defaultEncoding = System.getProperty("file.encoding", "UTF-8");
        zoneId = ZoneId.of("Europe/Moscow"); // TODO change to ZoneId.systemDefault();
        ownNickName = "You";
        recursiveSearch = false;
        combineHistories = false;
        searchExtensions = new String[] {"qhf", "ahf"};
    }

    public static String getNoCodepageFound() {
        return String.format(Configuration.noCodepageFound, Configuration.defaultEncoding);
    }

}
