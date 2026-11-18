package converter;

import java.util.Locale;

/** Formats conversion results so they are easy to read (no long trails of zeros). */
public final class NumberFormatUtil {
    private NumberFormatUtil() {
    }

    public static String format(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "—";
        }
        double abs = Math.abs(value);
        String raw;
        if (abs == 0) {
            return "0";
        } else if (abs >= 100000) {
            raw = String.format(Locale.US, "%.2f", value);
        } else if (abs >= 1000) {
            raw = String.format(Locale.US, "%.2f", value);
        } else if (abs >= 1) {
            raw = String.format(Locale.US, "%.6f", value);
        } else if (abs >= 0.0001) {
            raw = String.format(Locale.US, "%.8f", value);
        } else {
            raw = String.format(Locale.US, "%.6e", value);
            return raw;
        }
        if (raw.contains(".")) {
            raw = raw.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return raw;
    }

    public static String formatCurrency(double value, String currencyCode) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "—";
        }
        // Yen is typically shown without fraction digits
        if ("JPY".equalsIgnoreCase(currencyCode)) {
            return String.format(Locale.US, "%,.0f", value);
        }
        return String.format(Locale.US, "%,.4f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
