package converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * INR-based exchange rates. Each stored number is "units of that currency per 1 INR".
 * The converter first loads the bundled offline file, then (if present) a
 * previously refreshed file in the data folder.
 */
public class CurrencyRates {
    public static final List<String> SUPPORTED = List.of(
            "INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "SGD", "AED", "CNY", "CHF", "NZD"
    );

    private final Map<String, Double> foreignPerInr = new LinkedHashMap<>();
    private String lastUpdated = "unknown";
    private String source = "offline";

    public CurrencyRates() {
        foreignPerInr.put("INR", 1.0);
    }

    public void setRate(String code, double foreignPerInrValue) {
        if (foreignPerInrValue <= 0 || Double.isNaN(foreignPerInrValue) || Double.isInfinite(foreignPerInrValue)) {
            return;
        }
        foreignPerInr.put(code.toUpperCase(), foreignPerInrValue);
    }

    public double getRate(String code) {
        Double value = foreignPerInr.get(code.toUpperCase());
        if (value == null) {
            throw new IllegalArgumentException("No exchange rate available for " + code + ".");
        }
        return value;
    }

    public boolean hasRate(String code) {
        return foreignPerInr.containsKey(code.toUpperCase());
    }

    /**
     * amountTo = amountFrom * (rateTo / rateFrom)
     * because both rates are expressed per 1 INR.
     */
    public double convert(double amount, String fromCode, String toCode) {
        return amount * getRate(toCode) / getRate(fromCode);
    }

    public List<String> getAvailableCodes() {
        List<String> codes = new ArrayList<>();
        for (String code : SUPPORTED) {
            if (hasRate(code)) {
                codes.add(code);
            }
        }
        // include any extra codes that came from a live refresh
        for (String code : foreignPerInr.keySet()) {
            if (!codes.contains(code)) {
                codes.add(code);
            }
        }
        return Collections.unmodifiableList(codes);
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getSource() {
        return source;
    }

    public void setMetadata(String lastUpdated, String source) {
        if (lastUpdated != null && !lastUpdated.isBlank()) {
            this.lastUpdated = lastUpdated;
        }
        if (source != null && !source.isBlank()) {
            this.source = source;
        }
    }

    public String statusText() {
        String src = "offline".equalsIgnoreCase(source) ? "offline fallback rates" : "live rates (" + source + ")";
        return "Using " + src + ", updated " + lastUpdated + ".";
    }

    public void load(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                if (trimmed.startsWith("#")) {
                    parseMetadata(trimmed);
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String code = trimmed.substring(0, eq).trim().toUpperCase();
                String number = trimmed.substring(eq + 1).trim();
                try {
                    setRate(code, Double.parseDouble(number));
                } catch (NumberFormatException ignored) {
                    // skip a bad line rather than failing the whole file
                }
            }
        }
        foreignPerInr.put("INR", 1.0);
    }

    public void save(Path file) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write("# Saved exchange rates (units of currency per 1 INR)\n");
            writer.write("# last_updated=" + lastUpdated + "\n");
            writer.write("# source=" + source + "\n");
            writer.write("# saved_at=" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\n");
            for (String code : getAvailableCodes()) {
                writer.write(code + "=" + foreignPerInr.get(code) + "\n");
            }
        }
    }

    private void parseMetadata(String commentLine) {
        String line = commentLine.substring(1).trim();
        if (line.startsWith("last_updated=")) {
            lastUpdated = line.substring("last_updated=".length()).trim();
        } else if (line.startsWith("source=")) {
            source = line.substring("source=".length()).trim();
        }
    }

    /**
     * Copies rates from a live API response, keeping any previous values
     * for currencies the API did not return.
     */
    public int mergeLiveRates(Map<String, Double> live, String sourceName, String updated) {
        int applied = 0;
        for (Map.Entry<String, Double> entry : live.entrySet()) {
            String code = entry.getKey().toUpperCase();
            if (!SUPPORTED.contains(code) && !"INR".equals(code)) {
                // keep the list focused on the currencies shown in the app
                if (!foreignPerInr.containsKey(code)) {
                    continue;
                }
            }
            setRate(code, entry.getValue());
            applied++;
        }
        foreignPerInr.put("INR", 1.0);
        setMetadata(updated == null || updated.isBlank()
                ? LocalDate.now().toString()
                : updated, sourceName);
        return applied;
    }
}
