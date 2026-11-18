package converter;

import java.util.List;

/**
 * A group of related units (length, weight, temperature, speed or time)
 * that can convert a value from one unit to another.
 */
public interface CategoryConverter {
    String getName();

    List<String> getUnitCodes();

    /** Short label shown in the drop-down, e.g. "Metre (m)". */
    String getLabel(String unitCode);

    double convert(double value, String fromCode, String toCode);
}
