import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

@Slf4j
public class Deduplicator implements Runnable {

    final static String[] ALLOWED_EXTENSIONS = new String[] { "JPEG", "JPG", "PNG", "jpeg", "jpg", "png", "mov",
                                                              "MOV", "MP4", "mp4" };
    final static boolean DO_RECURSIVE_SEARCH = true;

    @Parameters(paramLabel = "<folder-path>",
                description = "Path to the top folder which contains pictures to deduplicate.")
    private String pictureFolderPath;

    public static void main(String[] args) {
        BasicConfigurator.configure();
        int exitCode = new CommandLine(new Deduplicator()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        deduplicatePictureFilesInFolder(pictureFolderPath);
    }

    private void deduplicatePictureFilesInFolder(String pictureFolderPath) {
        String duplicatesPath = pictureFolderPath + "\\duplicates";
        createDuplicatesFolder(duplicatesPath);

        log.info("The following folder will be processed: {}", pictureFolderPath);

        Collection<File> files = FileUtils.listFiles(new File(pictureFolderPath), ALLOWED_EXTENSIONS,
                                                     DO_RECURSIVE_SEARCH);
        Map<MediaFile, Boolean> picFileToDeduplicatedMap = createInitialPicFileMapping(files);

        log.info("Number of all files available in directory: {}", files.size());
        boolean logTheFilesNames = true;
        if (logTheFilesNames) {
            log.info("The following files found in {} directory", pictureFolderPath);
            files.forEach(file -> log.info(file.getAbsolutePath()));
        }
        log.info("Number of all unique files available in directory: {}", picFileToDeduplicatedMap.keySet().size());
        int duplicateCounter = 1;
        for (File file : files) {
            MediaFile mediaFile = new MediaFile(file.lastModified(), FileUtils.sizeOf(file), file.getName());
            if (picFileToDeduplicatedMap.get(mediaFile)) {
                if (file.renameTo(
                    new File(duplicatesPath + "\\" + "Dupl" + duplicateCounter++ + "_" + file.getName()))) {
                    log.info("{} was successfully moved to 'duplicates' directory.", file.getName());
                } else {
                    log.error("Failed to move {} to 'duplicates' directory", file.getName());
                }
            } else {
                picFileToDeduplicatedMap.put(mediaFile, true);
            }
        }
    }

    private void createDuplicatesFolder(String duplicatesPath) {
        if (new File(duplicatesPath).exists()) {
            log.info("Folder for duplicate files already exists. It'll be removed.");
            try {
                log.info("Trying to delete.");
                FileUtils.deleteDirectory(new File(duplicatesPath));
                log.info("Deleted.");
            } catch (IOException e) {
                log.info("There were issue with deleting the 'duplicates' directory.");
                e.printStackTrace();
            }
        }
        if (new File(duplicatesPath).mkdirs()) {
            log.info("Folder for duplicate files was created successfully: {}", duplicatesPath);
        } else {
            log.error("Folder for duplicate files was not created!");
        }
    }

    private HashMap<MediaFile, Boolean> createInitialPicFileMapping(Collection<File> files) {
        HashMap<MediaFile, Boolean> picFileToDeduplicatedMap = new HashMap<>();
        for (File file : files) {
            picFileToDeduplicatedMap.put(new MediaFile(file.lastModified(), FileUtils.sizeOf(file), file.getName()),
                                         false);
        }
        return picFileToDeduplicatedMap;
    }
}
