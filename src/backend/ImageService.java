package backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ImageService {
    private final File imagesFolder = new File("repo/images");

    public ImageService() {
        if (!imagesFolder.exists()) imagesFolder.mkdirs();
    }

    public String saveImage(File sourceFile) throws IOException {
        String name = sourceFile.getName();
        File targetFile = new File(imagesFolder, name);

        Files.copy(
            sourceFile.toPath(),
            targetFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
            );
        return "images/" + name;
    }

    public void deleteImage(String fileName) {
        File f = new File("repo/images", fileName);
        if (f.exists()) f.delete();
    }

    public boolean exists(String fileName) {
        File f = new File("repo/images", fileName);
        return f.exists();
    }
}
