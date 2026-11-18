package converter.ui;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

/** Paints the window (or a tab) to a PNG. Used to create README screenshots. */
public final class ScreenshotUtil {
    private ScreenshotUtil() {
    }

    public static void capture(JComponent component, Path file) throws Exception {
        Files.createDirectories(file.getParent());
        // Let the layout finish before painting
        component.revalidate();
        component.repaint();
        SwingUtilities.invokeAndWait(() -> { /* flush EDT */ });
        Thread.sleep(200);

        int width = Math.max(component.getWidth(), 1);
        int height = Math.max(component.getHeight(), 1);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(() -> component.paint(image.getGraphics()));
        ImageIO.write(image, "png", file.toFile());
        System.out.println("Saved " + file.toAbsolutePath());
    }

    public static void selectTab(JTabbedPane tabs, String title) {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (title.equalsIgnoreCase(tabs.getTitleAt(i))) {
                tabs.setSelectedIndex(i);
                return;
            }
        }
    }
}
