package converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One line in the conversion history panel. */
public final class ConversionRecord {
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final LocalDateTime time;
    private final String category;
    private final String displayText;

    public ConversionRecord(String category, String displayText) {
        this.time = LocalDateTime.now();
        this.category = category;
        this.displayText = displayText;
    }

    public String getCategory() {
        return category;
    }

    public String getDisplayText() {
        return displayText;
    }

    @Override
    public String toString() {
        return time.format(TIME) + "  [" + category + "]  " + displayText;
    }
}
