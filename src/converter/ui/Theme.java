package converter.ui;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

/** Shared colours and fonts for the window. */
public final class Theme {
    public static final Color HEADER_BG = new Color(0x1F4E5F);
    public static final Color HEADER_BG_END = new Color(0x2A6F7F);
    public static final Color ACCENT = new Color(0x0E7490);
    public static final Color ACCENT_DARK = new Color(0x155E75);
    public static final Color PAGE_BG = new Color(0xF3F6F8);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT = new Color(0x1F2933);
    public static final Color MUTED = new Color(0x52606D);
    public static final Color LINE = new Color(0xD9E2EC);
    public static final Color ERROR = new Color(0xB91C1C);
    public static final Color SUCCESS = new Color(0x0F766E);
    public static final Color BUTTON_TEXT = Color.WHITE;

    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font SUBTITLE = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font LABEL = new Font("SansSerif", Font.BOLD, 13);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font SMALL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font BUTTON = new Font("SansSerif", Font.BOLD, 13);

    private Theme() {
    }

    public static void applyLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // keep the default look if Nimbus is not available
        }
        UIManager.put("control", PAGE_BG);
        UIManager.put("nimbusBase", ACCENT_DARK);
        UIManager.put("nimbusFocus", ACCENT);
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(16, 18, 16, 18));
    }
}
