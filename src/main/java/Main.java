import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {

        String basePath = "E:\\!Temp\\210420\\History";

        List<Path> pathList = getPathList(basePath, true);
        try {
            for (Path path : pathList) {
                Chat chat = QhfParser.parseQhfFile(path);
                String fileName = path.getFileName().toString().toLowerCase()
                        .replace(".qhf", ".txt").replace(".ahf", ".txt");
                Path outPath = Paths.get(path.getParent().toString(), fileName);
                ChatStatistics.collectStatistics(chat);
                QhfParser.saveChatToTxt(chat, outPath);
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ChatStatistics.printStatistics();
       // pathList.stream().map(Path::getFileName).forEach(System.out::println);

        // Path::getParent - directory

        // Path::getFileName - file name
        //System.out.println(pathList.toString());

    }

    private static List<Path> getPathList(String basePath, boolean isRecursively) {
        List<Path> files = null;
        try {
            files = Files.find(Paths.get(basePath), isRecursively ? Integer.MAX_VALUE : 1,
                    ((path, basicFileAttributes) -> basicFileAttributes.isRegularFile()))
                    .filter(file -> {
                                String fileName = file.getFileName().toString().toLowerCase();
                                return fileName.endsWith(".qhf") || fileName.endsWith(".ahf");
                            }
                    )
                    .collect(Collectors.toList());

        } catch (IOException e) {
            System.out.println(String.format(Configuration.noFilesFound, basePath));
            e.printStackTrace();
        }
        return files;
    }

    private static void showHelp() {
        System.out.println("QIP history (*.qhf or *.ahf) converter to *.txt files. Usage:");
        System.out.println("QIP history (*.qhf or *.ahf) converter to *.txt files. Usage:");
    }

}
