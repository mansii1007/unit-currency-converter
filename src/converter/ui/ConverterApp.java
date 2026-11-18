package converter.ui;

import converter.CurrencyConverter;

import javax.swing.SwingUtilities;
import javax.swing.JTabbedPane;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point for the Unit & Currency Converter.
 *
 *   java -cp out converter.ui.ConverterApp
 *   java -cp out converter.ui.ConverterApp --screenshot screenshots
 */
public class ConverterApp {
    public static void main(String[] args) {
        Theme.applyLookAndFeel();
        Path screenshotDir = null;
        for (int i = 0; i < args.length; i++) {
            if ("--screenshot".equals(args[i]) && i + 1 < args.length) {
                screenshotDir = Paths.get(args[++i]);
            } else if ("--help".equals(args[i]) || "-h".equals(args[i])) {
                System.out.println("Unit & Currency Converter");
                System.out.println("  --screenshot <folder>   Save PNG previews of each tab and exit");
                return;
            }
        }

        CurrencyConverter currency = CurrencyConverter.loadDefault();
        Path shots = screenshotDir;
        SwingUtilities.invokeLater(() -> {
            ConverterFrame frame = new ConverterFrame(currency);
            frame.setVisible(true);
            if (shots != null) {
                new Thread(() -> {
                    try {
                        captureScreenshots(frame, shots);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                    System.exit(0);
                }, "screenshot-runner").start();
            }
        });
    }

    private static void captureScreenshots(ConverterFrame frame, Path dir) throws Exception {
        Thread.sleep(400);
        JTabbedPane tabs = frame.getTabs();

        SwingUtilities.invokeAndWait(() -> {
            ScreenshotUtil.selectTab(tabs, "Length");
            frame.getLengthPanel().setSample("100", "cm", "m");
        });
        ScreenshotUtil.capture(frame.getRootPane(), dir.resolve("length.png"));

        SwingUtilities.invokeAndWait(() -> {
            ScreenshotUtil.selectTab(tabs, "Weight");
            frame.getWeightPanel().setSample("1", "kg", "lb");
        });
        ScreenshotUtil.capture(frame.getRootPane(), dir.resolve("weight.png"));

        SwingUtilities.invokeAndWait(() -> {
            ScreenshotUtil.selectTab(tabs, "Temperature");
            frame.getTemperaturePanel().setSample("32", "F", "C");
        });
        ScreenshotUtil.capture(frame.getRootPane(), dir.resolve("temperature.png"));

        SwingUtilities.invokeAndWait(() -> {
            ScreenshotUtil.selectTab(tabs, "Speed / Time");
            frame.getSpeedTimePanel().getSpeedPanel().setSample("100", "km/h", "m/s");
        });
        ScreenshotUtil.capture(frame.getRootPane(), dir.resolve("speed-time.png"));

        SwingUtilities.invokeAndWait(() -> {
            ScreenshotUtil.selectTab(tabs, "Currency");
            frame.getCurrencyPanel().setSample("1000", "INR", "USD");
        });
        try {
            frame.getCurrencyPanel().refreshRatesAndWait(10_000);
        } catch (Exception ex) {
            System.out.println("Live rate refresh skipped or failed: " + ex.getMessage());
        }
        Thread.sleep(300);
        ScreenshotUtil.capture(frame.getRootPane(), dir.resolve("currency.png"));
        System.out.println("All screenshots written to " + dir.toAbsolutePath());
    }
}
