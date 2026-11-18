package converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Newest-first list of recent conversions, capped so the panel stays readable. */
public class ConversionHistory {
    public static final int MAX_ENTRIES = 40;

    private final List<ConversionRecord> records = new ArrayList<>();

    public void add(ConversionRecord record) {
        records.add(0, record);
        while (records.size() > MAX_ENTRIES) {
            records.remove(records.size() - 1);
        }
    }

    public void add(String category, String displayText) {
        add(new ConversionRecord(category, displayText));
    }

    public void clear() {
        records.clear();
    }

    public List<ConversionRecord> getRecords() {
        return Collections.unmodifiableList(records);
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }
}
