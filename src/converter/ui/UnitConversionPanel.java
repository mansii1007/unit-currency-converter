package converter.ui;

import converter.CategoryConverter;
import converter.ConversionHistory;
import converter.InputValidator;
import converter.NumberFormatUtil;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Reusable panel for length, weight, temperature, speed and time.
 * The conversion logic lives in {@link CategoryConverter}; this class only draws the form.
 */
public class UnitConversionPanel extends JPanel {
    private final CategoryConverter converter;
    private final ConversionHistory history;
    private final HistoryPanel historyPanel;
    private final boolean allowNegative;

    private final JTextField fromField = new JTextField();
    private final JTextField toField = new JTextField();
    private final JComboBox<UnitItem> fromUnit = new JComboBox<>();
    private final JComboBox<UnitItem> toUnit = new JComboBox<>();
    private final JLabel message = new JLabel(" ");
    private boolean updating;

    public UnitConversionPanel(CategoryConverter converter, ConversionHistory history,
                               HistoryPanel historyPanel, boolean allowNegative) {
        this.converter = converter;
        this.history = history;
        this.historyPanel = historyPanel;
        this.allowNegative = allowNegative;

        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 10, 16));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.CARD_BG);
        card.setBorder(Theme.cardBorder());

        fillUnits(fromUnit);
        fillUnits(toUnit);
        if (fromUnit.getItemCount() > 0) {
            fromUnit.setSelectedIndex(0);
        }
        if (toUnit.getItemCount() > 1) {
            toUnit.setSelectedIndex(1);
        } else if (toUnit.getItemCount() > 0) {
            toUnit.setSelectedIndex(0);
        }

        fromField.setFont(Theme.BODY);
        toField.setFont(Theme.BODY);
        fromField.setPreferredSize(new Dimension(180, 32));
        toField.setPreferredSize(new Dimension(180, 32));
        toField.setEditable(false);
        toField.setBackground(new Color(0xF8FAFC));
        fromUnit.setFont(Theme.BODY);
        toUnit.setFont(Theme.BODY);
        fromUnit.setPreferredSize(new Dimension(220, 32));
        toUnit.setPreferredSize(new Dimension(220, 32));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        card.add(label("From"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        card.add(fromField, c);
        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        card.add(fromUnit, c);

        JButton swap = new JButton("Swap  \u2194");
        swap.setFont(Theme.BUTTON);
        swap.setToolTipText("Swap the two units");
        swap.addActionListener(e -> swapUnits());

        c.gridx = 0; c.gridy = 1;
        card.add(label("To"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        card.add(toField, c);
        c.gridx = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        card.add(toUnit, c);

        JButton convert = new JButton("Convert");
        convert.setFont(Theme.BUTTON);
        convert.setBackground(Theme.ACCENT);
        convert.setForeground(Theme.BUTTON_TEXT);
        convert.setOpaque(true);
        convert.setBorderPainted(false);
        convert.setPreferredSize(new Dimension(120, 34));
        convert.addActionListener(e -> convert(true));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(convert);
        actions.add(swap);

        message.setFont(Theme.SMALL);
        message.setForeground(Theme.MUTED);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 3; c.fill = GridBagConstraints.HORIZONTAL;
        card.add(actions, c);
        c.gridy = 3;
        card.add(message, c);

        add(card, BorderLayout.NORTH);

        fromField.addActionListener(e -> convert(true));
        fromUnit.addActionListener(e -> convert(false));
        toUnit.addActionListener(e -> convert(false));
        fromField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { convert(false); }
            @Override public void removeUpdate(DocumentEvent e) { convert(false); }
            @Override public void changedUpdate(DocumentEvent e) { convert(false); }
        });
    }

    public void setSample(String value, String from, String to) {
        updating = true;
        fromField.setText(value);
        selectUnit(fromUnit, from);
        selectUnit(toUnit, to);
        updating = false;
        convert(true);
    }

    private void fillUnits(JComboBox<UnitItem> box) {
        DefaultComboBoxModel<UnitItem> model = new DefaultComboBoxModel<>();
        List<String> codes = converter.getUnitCodes();
        for (String code : codes) {
            model.addElement(new UnitItem(code, converter.getLabel(code)));
        }
        box.setModel(model);
    }

    private void selectUnit(JComboBox<UnitItem> box, String code) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).code.equals(code)) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private void swapUnits() {
        UnitItem left = (UnitItem) fromUnit.getSelectedItem();
        UnitItem right = (UnitItem) toUnit.getSelectedItem();
        String result = toField.getText();
        updating = true;
        fromUnit.setSelectedItem(right);
        toUnit.setSelectedItem(left);
        if (result != null && !result.isBlank() && !"—" .equals(result)) {
            fromField.setText(result.replace(",", ""));
        }
        updating = false;
        convert(true);
    }

    private void convert(boolean logToHistory) {
        if (updating) {
            return;
        }
        String raw = fromField.getText();
        if (raw == null || raw.trim().isEmpty()) {
            toField.setText("");
            if (logToHistory) {
                showError("Please enter a value to convert.");
            } else {
                clearMessage();
            }
            return;
        }
        try {
            double value = allowNegative
                    ? InputValidator.parseTemperature(raw)
                    : InputValidator.parseNonNegative(raw);
            UnitItem from = (UnitItem) fromUnit.getSelectedItem();
            UnitItem to = (UnitItem) toUnit.getSelectedItem();
            if (from == null || to == null) {
                return;
            }
            double result = converter.convert(value, from.code, to.code);
            String formatted = NumberFormatUtil.format(result);
            toField.setText(formatted);
            clearMessage();
            if (logToHistory) {
                String text = NumberFormatUtil.format(value) + " " + from.code
                        + "  \u2192  " + formatted + " " + to.code;
                history.add(converter.getName(), text);
                historyPanel.refresh();
                message.setForeground(Theme.SUCCESS);
                message.setText("Converted successfully.");
            }
        } catch (IllegalArgumentException ex) {
            toField.setText("");
            if (logToHistory || (raw != null && !raw.trim().isEmpty() && !raw.trim().endsWith("."))) {
                showError(ex.getMessage());
            }
        }
    }

    private void showError(String text) {
        message.setForeground(Theme.ERROR);
        message.setText(text);
    }

    private void clearMessage() {
        message.setForeground(Theme.MUTED);
        message.setText(" ");
    }

    private static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.LABEL);
        label.setForeground(Theme.TEXT);
        label.setPreferredSize(new Dimension(50, 24));
        return label;
    }

    static final class UnitItem {
        final String code;
        final String label;

        UnitItem(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
