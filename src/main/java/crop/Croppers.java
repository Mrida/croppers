package crop;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Croppers {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Folder should be specified as param");
            return;
        }
        File dir = new File(args[0]);
        String DESTINATION_SUBDIR = args.length > 1 ? args[1] : "cropped";
        if (!dir.isDirectory()) {
            System.out.println("input should be a directory");
            return;
        }

        File croppedDir = new File(Paths.get(dir.getAbsolutePath(), DESTINATION_SUBDIR).toUri());
        if (!croppedDir.exists()) {
            croppedDir.mkdir();
        }
        for (File file : dir.listFiles()) {
            if (!file.isDirectory()) {
                if (isImage(file)) {
                    final BufferedImage original = ImageIO.read(file);
                    final BufferedImage cropped = original.getSubimage(0, 74, original.getWidth() - 74, original.getHeight() - 74);

                    final Path dest = Files.createFile(Paths.get(dir.getAbsolutePath(), DESTINATION_SUBDIR, file.getName()));
                    final File destFile = new File(dest.toUri());
                    ImageIO.write(cropped, "png", destFile);
                } else {
                    System.out.println(file.getName() + " was skipped as it is not an image");
                }
            }
        }

    }

    private static boolean isImage(File file) {
        final String substring = file.getName().substring(file.getName().lastIndexOf('.')+1);
        return substring.equalsIgnoreCase("png") ||
               substring.equalsIgnoreCase("jpg") ||
               substring.equalsIgnoreCase("jpeg");
    }
}
