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
    public static final String noFilesFound = "No files were found in provided path %s.";
    public static final String messageWithZeroLength = "[QhtToTxt] No message available - message length is zero.";
    public static final String tryingToSaveCorruptedFile = "Seems like the file %s is damaged. " +
            "Trying to save %d out of %d messages.";
    public static final String messageIsCorrupted = "[QhtToTxt] Probably this message is corrupted. " +
            "There are %d bytes lost.";
    public static final String noPathFound = "The file or path %s does not exist.";
    public static final String analizingFolders = "Seeking for .qhf or .ahf files...";
    public static final String startToReadFiles = "Reading files...";
    public static final String done = "Done.";
    public static final String foundNfiles = "Found %d files.";
    public static final String combiningFiles = "Combining files...";
    public static final String deletingIdenticalMessages = "Deleting identical messages...";
    public static final String sortingMessages = "Putting all messages back together...";
    public static final String savingFiles = "Saving history...";

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
