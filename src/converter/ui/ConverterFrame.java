package converter.ui;

import converter.ConversionHistory;
import converter.CurrencyConverter;
import converter.FactorConverter;
import converter.TemperatureConverter;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Main application window. */
public class ConverterFrame extends JFrame {
    private final JTabbedPane tabs = new JTabbedPane();
    private final UnitConversionPanel lengthPanel;
    private final UnitConversionPanel weightPanel;
    private final UnitConversionPanel temperaturePanel;
    private final SpeedTimePanel speedTimePanel;
    private final CurrencyPanel currencyPanel;
    private final HistoryPanel historyPanel;

    public ConverterFrame(CurrencyConverter currencyConverter) {
        super("Unit & Currency Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 580));

        ConversionHistory history = new ConversionHistory();
        historyPanel = new HistoryPanel(history);

        lengthPanel = new UnitConversionPanel(FactorConverter.length(), history, historyPanel, false);
        weightPanel = new UnitConversionPanel(FactorConverter.weight(), history, historyPanel, false);
        temperaturePanel = new UnitConversionPanel(new TemperatureConverter(), history, historyPanel, true);
        speedTimePanel = new SpeedTimePanel(history, historyPanel);
        currencyPanel = new CurrencyPanel(currencyConverter, history, historyPanel);

        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("Length", lengthPanel);
        tabs.addTab("Weight", weightPanel);
        tabs.addTab("Temperature", temperaturePanel);
        tabs.addTab("Speed / Time", speedTimePanel);
        tabs.addTab("Currency", currencyPanel);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.PAGE_BG);
        root.add(new HeaderPanel(), BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(historyPanel, BorderLayout.SOUTH);
        setContentPane(root);
        pack();
        setSize(860, 640);
        setLocationRelativeTo(null);
    }

    public JTabbedPane getTabs() {
        return tabs;
    }

    public UnitConversionPanel getLengthPanel() {
        return lengthPanel;
    }

    public UnitConversionPanel getWeightPanel() {
        return weightPanel;
    }

    public UnitConversionPanel getTemperaturePanel() {
        return temperaturePanel;
    }

    public SpeedTimePanel getSpeedTimePanel() {
        return speedTimePanel;
    }

    public CurrencyPanel getCurrencyPanel() {
        return currencyPanel;
    }

    /** Gradient title bar at the top of the window. */
    private static class HeaderPanel extends JPanel {
        HeaderPanel() {
            setPreferredSize(new Dimension(800, 78));
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 22, 14, 22));
            JLabel title = new JLabel("Unit & Currency Converter");
            title.setFont(Theme.TITLE);
            title.setForeground(Color.WHITE);
            JLabel subtitle = new JLabel("Length  ·  Weight  ·  Temperature  ·  Speed  ·  Time  ·  Currency");
            subtitle.setFont(Theme.SUBTITLE);
            subtitle.setForeground(new Color(255, 255, 255, 210));
            JPanel text = new JPanel(new BorderLayout(0, 4));
            text.setOpaque(false);
            text.add(title, BorderLayout.NORTH);
            text.add(subtitle, BorderLayout.SOUTH);
            add(text, BorderLayout.WEST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, Theme.HEADER_BG, getWidth(), getHeight(), Theme.HEADER_BG_END));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
