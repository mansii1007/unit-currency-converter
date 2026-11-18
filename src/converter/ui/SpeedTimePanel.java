package converter.ui;

import converter.CategoryConverter;
import converter.ConversionHistory;
import converter.FactorConverter;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

/** One tab that holds both Speed and Time converters. */
public class SpeedTimePanel extends JPanel {
    private final UnitConversionPanel speedPanel;
    private final UnitConversionPanel timePanel;

    public SpeedTimePanel(ConversionHistory history, HistoryPanel historyPanel) {
        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);
        CategoryConverter speed = FactorConverter.speed();
        CategoryConverter time = FactorConverter.time();
        speedPanel = new UnitConversionPanel(speed, history, historyPanel, false);
        timePanel = new UnitConversionPanel(time, history, historyPanel, false);
        JTabbedPane inner = new JTabbedPane();
        inner.addTab("Speed", speedPanel);
        inner.addTab("Time", timePanel);
        add(inner, BorderLayout.CENTER);
    }

    public UnitConversionPanel getSpeedPanel() {
        return speedPanel;
    }

    public UnitConversionPanel getTimePanel() {
        return timePanel;
    }
}
