package converter.ui;

import converter.ConversionHistory;
import converter.ConversionRecord;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/** Shared conversion history shown at the bottom of the window. */
public class HistoryPanel extends JPanel {
    private final ConversionHistory history;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);

    public HistoryPanel(ConversionHistory history) {
        this.history = history;
        setLayout(new BorderLayout(8, 8));
        setBackground(Theme.PAGE_BG);
        setBorder(BorderFactory.createEmptyBorder(0, 16, 14, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Conversion history");
        title.setFont(Theme.LABEL);
        title.setForeground(Theme.TEXT);
        header.add(title, BorderLayout.WEST);

        JButton clear = new JButton("Clear");
        clear.setFont(Theme.SMALL);
        clear.addActionListener(e -> {
            history.clear();
            refresh();
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(clear);
        header.add(right, BorderLayout.EAST);

        list.setFont(Theme.SMALL);
        list.setForeground(Theme.TEXT);
        list.setBackground(Theme.CARD_BG);
        list.setVisibleRowCount(5);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(100, 110));
        scroll.setBorder(BorderFactory.createLineBorder(Theme.LINE));

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        model.clear();
        if (history.isEmpty()) {
            model.addElement("No conversions yet. Enter a value and press Convert.");
            return;
        }
        for (ConversionRecord record : history.getRecords()) {
            model.addElement(record.toString());
        }
    }
}
