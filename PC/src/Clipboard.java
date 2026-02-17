import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

/*Clipboard.java
CLipboard accessing and setting
    - getClipboard()
    - setClipboard()
*/
public class Clipboard {
    java.awt.datatransfer.Clipboard clipboard;

    Clipboard() {
        this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    @SuppressWarnings("unchecked")
    String getClipboard() throws Exception {
        try {
            if (clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
                System.out.println("Image");
                Image image = (Image) clipboard.getData(DataFlavor.imageFlavor);

                BufferedImage buffered = new BufferedImage(
                        image.getWidth(null),
                        image.getHeight(null),
                        BufferedImage.TYPE_INT_ARGB);

                Graphics2D g = buffered.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(buffered, "png", baos);

                String encoded = Base64.getEncoder()
                        .encodeToString(baos.toByteArray());

                return "~~" + encoded;

            } else if (clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {

                List<File> files = (List<File>) clipboard.getData(DataFlavor.javaFileListFlavor);

                File img = files.get(0); // usually first image
                byte[] bytes = Files.readAllBytes(img.toPath());

                String encoded = Base64.getEncoder().encodeToString(bytes);
                return "~~" + encoded;
            } else if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {

                return (String) clipboard.getData(DataFlavor.stringFlavor);
            }

            return "";

        } catch (Exception e) {
            LogManager.error("Error reading clipboard", e);
            throw e;
        }
    }

    void setClipboard(String text) {
        try {
            StringSelection selection = new StringSelection(text);
            this.clipboard.setContents(selection, null);
            LogManager.paste(text);
        } catch (Exception e) {
            LogManager.error("Error setting clipboard", e);
        }
    }
}
