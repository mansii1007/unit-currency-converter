package converter.ui;

import converter.ConversionHistory;
import converter.CurrencyConverter;
import converter.InputValidator;
import converter.NumberFormatUtil;
import converter.RefreshResult;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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
 * Currency tab: offline INR-based rates plus an optional live refresh.
 * Rate fetching runs on a background thread so the window does not freeze.
 */
public class CurrencyPanel extends JPanel {
    private final CurrencyConverter converter;
    private final ConversionHistory history;
    private final HistoryPanel historyPanel;

    private final JTextField fromField = new JTextField();
    private final JTextField toField = new JTextField();
    private final JComboBox<UnitConversionPanel.UnitItem> fromUnit = new JComboBox<>();
    private final JComboBox<UnitConversionPanel.UnitItem> toUnit = new JComboBox<>();
    private final JLabel message = new JLabel(" ");
    private final JLabel status = new JLabel();
    private final JButton refreshButton = new JButton("Refresh live rates");
    private boolean updating;

    public CurrencyPanel(CurrencyConverter converter, ConversionHistory history, HistoryPanel historyPanel) {
        this.converter = converter;
        this.history = history;
        this.historyPanel = historyPanel;

        setLayout(new BorderLayout());
        setBackground(Theme.PAGE_BG);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 10, 16));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.CARD_BG);
        card.setBorder(Theme.cardBorder());

        reloadUnitLists();
        select("INR", fromUnit);
        select("USD", toUnit);

        fromField.setFont(Theme.BODY);
        toField.setFont(Theme.BODY);
        fromField.setPreferredSize(new Dimension(180, 32));
        toField.setPreferredSize(new Dimension(180, 32));
        toField.setEditable(false);
        toField.setBackground(new Color(0xF8FAFC));
        fromUnit.setFont(Theme.BODY);
        toUnit.setFont(Theme.BODY);
        fromUnit.setPreferredSize(new Dimension(240, 32));
        toUnit.setPreferredSize(new Dimension(240, 32));

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
        swap.setToolTipText("Swap the two currencies");
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

        refreshButton.setFont(Theme.BUTTON);
        refreshButton.setToolTipText("Download current rates from the internet. Falls back to the saved offline file if that fails.");
        refreshButton.addActionListener(e -> refreshRates());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(convert);
        actions.add(swap);
        actions.add(refreshButton);

        status.setFont(Theme.SMALL);
        status.setForeground(Theme.MUTED);
        status.setText(converter.getRates().statusText());

        message.setFont(Theme.SMALL);
        message.setForeground(Theme.MUTED);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 3; c.fill = GridBagConstraints.HORIZONTAL;
        card.add(actions, c);
        c.gridy = 3;
        card.add(status, c);
        c.gridy = 4;
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
        select(from, fromUnit);
        select(to, toUnit);
        updating = false;
        convert(true);
    }

    private void reloadUnitLists() {
        fill(fromUnit);
        fill(toUnit);
    }

    private void fill(JComboBox<UnitConversionPanel.UnitItem> box) {
        String previous = selectedCode(box);
        DefaultComboBoxModel<UnitConversionPanel.UnitItem> model = new DefaultComboBoxModel<>();
        List<String> codes = converter.getUnitCodes();
        for (String code : codes) {
            model.addElement(new UnitConversionPanel.UnitItem(code, converter.getLabel(code)));
        }
        box.setModel(model);
        if (previous != null) {
            select(previous, box);
        }
    }

    private void select(String code, JComboBox<UnitConversionPanel.UnitItem> box) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).code.equals(code)) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    private String selectedCode(JComboBox<UnitConversionPanel.UnitItem> box) {
        UnitConversionPanel.UnitItem item = (UnitConversionPanel.UnitItem) box.getSelectedItem();
        return item == null ? null : item.code;
    }

    private void swapUnits() {
        UnitConversionPanel.UnitItem left = (UnitConversionPanel.UnitItem) fromUnit.getSelectedItem();
        UnitConversionPanel.UnitItem right = (UnitConversionPanel.UnitItem) toUnit.getSelectedItem();
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
                showError("Please enter an amount to convert.");
            } else {
                message.setText(" ");
            }
            return;
        }
        try {
            double value = InputValidator.parseNonNegative(raw);
            UnitConversionPanel.UnitItem from = (UnitConversionPanel.UnitItem) fromUnit.getSelectedItem();
            UnitConversionPanel.UnitItem to = (UnitConversionPanel.UnitItem) toUnit.getSelectedItem();
            if (from == null || to == null) {
                return;
            }
            double result = converter.convert(value, from.code, to.code);
            String formatted = NumberFormatUtil.formatCurrency(result, to.code);
            toField.setText(formatted);
            message.setForeground(Theme.MUTED);
            message.setText(" ");
            if (logToHistory) {
                String text = NumberFormatUtil.formatCurrency(value, from.code) + " " + from.code
                        + "  \u2192  " + formatted + " " + to.code;
                history.add("Currency", text);
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

    private void refreshRates() {
        refreshButton.setEnabled(false);
        status.setForeground(Theme.MUTED);
        status.setText("Refreshing live rates…");
        new SwingWorker<RefreshResult, Void>() {
            @Override
            protected RefreshResult doInBackground() {
                return converter.refreshLiveRates();
            }

            @Override
            protected void done() {
                RefreshResult result;
                try {
                    result = get();
                } catch (Exception ex) {
                    result = RefreshResult.fail("Could not refresh rates. Offline rates will be used instead.");
                }
                status.setText(converter.getRates().statusText());
                if (result.isSuccess()) {
                    status.setForeground(Theme.SUCCESS);
                    message.setForeground(Theme.SUCCESS);
                    message.setText(result.getMessage());
                    String from = selectedCode(fromUnit);
                    String to = selectedCode(toUnit);
                    updating = true;
                    reloadUnitLists();
                    if (from != null) {
                        select(from, fromUnit);
                    }
                    if (to != null) {
                        select(to, toUnit);
                    }
                    updating = false;
                    convert(false);
                } else {
                    status.setForeground(Theme.MUTED);
                    message.setForeground(Theme.ERROR);
                    message.setText(result.getMessage());
                }
                refreshButton.setEnabled(true);
            }
        }.execute();
    }

    /**
     * Used by the screenshot helper so it can wait until a refresh finishes.
     * Returns immediately if the call is already on the event thread after completion.
     */
    public void refreshRatesAndWait(long timeoutMs) throws Exception {
        final Object lock = new Object();
        final boolean[] done = {false};
        final Exception[] error = {null};
        SwingUtilities.invokeAndWait(() -> {
            refreshButton.setEnabled(false);
            status.setText("Refreshing live rates…");
        });
        RefreshResult result = converter.refreshLiveRates();
        SwingUtilities.invokeAndWait(() -> {
            try {
                status.setText(converter.getRates().statusText());
                if (result.isSuccess()) {
                    status.setForeground(Theme.SUCCESS);
                    message.setForeground(Theme.SUCCESS);
                    message.setText(result.getMessage());
                    convert(false);
                } else {
                    message.setForeground(Theme.ERROR);
                    message.setText(result.getMessage());
                }
            } catch (Exception ex) {
                error[0] = ex;
            } finally {
                refreshButton.setEnabled(true);
                synchronized (lock) {
                    done[0] = true;
                    lock.notifyAll();
                }
            }
        });
        if (error[0] != null) {
            throw error[0];
        }
    }

    private void showError(String text) {
        message.setForeground(Theme.ERROR);
        message.setText(text);
    }

    private static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.LABEL);
        label.setForeground(Theme.TEXT);
        label.setPreferredSize(new Dimension(50, 24));
        return label;
    }
}
