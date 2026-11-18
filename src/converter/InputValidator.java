package converter;

/**
 * Shared checks for numbers typed into the converter.
 * Temperature may be negative; every other category must be zero or positive.
 */
public final class InputValidator {
    private InputValidator() {
    }

    public static double parseNumber(String raw) throws IllegalArgumentException {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a value to convert.");
        }
        String text = raw.trim().replace(",", "");
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("\"" + raw.trim() + "\" is not a valid number.");
        }
    }

    public static double parseNonNegative(String raw) throws IllegalArgumentException {
        double value = parseNumber(raw);
        if (value < 0) {
            throw new IllegalArgumentException("Please enter a value that is zero or greater.");
        }
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new IllegalArgumentException("Please enter a finite number.");
        }
        return value;
    }

    public static double parseTemperature(String raw) throws IllegalArgumentException {
        double value = parseNumber(raw);
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new IllegalArgumentException("Please enter a finite number.");
        }
        return value;
    }
}
