package converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converter for units that are simple multiples of a base unit
 * (length, weight, speed, time). The stored factor is
 * "how many base units equal 1 of this unit".
 *
 * Example (base = metre): metre = 1, kilometre = 1000, centimetre = 0.01.
 * Formula: result = value * factor(from) / factor(to).
 */
public class FactorConverter implements CategoryConverter {
    private final String name;
    private final Map<String, Double> factors = new LinkedHashMap<>();
    private final Map<String, String> labels = new LinkedHashMap<>();

    public FactorConverter(String name) {
        this.name = name;
    }

    public FactorConverter add(String code, String label, double factorToBase) {
        if (factorToBase == 0) {
            throw new IllegalArgumentException("Factor for " + code + " cannot be zero.");
        }
        factors.put(code, factorToBase);
        labels.put(code, label);
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<String> getUnitCodes() {
        return Collections.unmodifiableList(new ArrayList<>(factors.keySet()));
    }

    @Override
    public String getLabel(String unitCode) {
        return labels.getOrDefault(unitCode, unitCode);
    }

    @Override
    public double convert(double value, String fromCode, String toCode) {
        Double from = factors.get(fromCode);
        Double to = factors.get(toCode);
        if (from == null) {
            throw new IllegalArgumentException("Unknown unit: " + fromCode);
        }
        if (to == null) {
            throw new IllegalArgumentException("Unknown unit: " + toCode);
        }
        return value * from / to;
    }

    public static FactorConverter length() {
        return new FactorConverter("Length")
                .add("mm", "Millimetre (mm)", 0.001)
                .add("cm", "Centimetre (cm)", 0.01)
                .add("m", "Metre (m)", 1.0)
                .add("km", "Kilometre (km)", 1000.0)
                .add("in", "Inch (in)", 0.0254)
                .add("ft", "Foot (ft)", 0.3048)
                .add("yd", "Yard (yd)", 0.9144)
                .add("mi", "Mile (mi)", 1609.344);
    }

    public static FactorConverter weight() {
        return new FactorConverter("Weight")
                .add("mg", "Milligram (mg)", 0.000001)
                .add("g", "Gram (g)", 0.001)
                .add("kg", "Kilogram (kg)", 1.0)
                .add("t", "Metric tonne (t)", 1000.0)
                .add("oz", "Ounce (oz)", 0.028349523125)
                .add("lb", "Pound (lb)", 0.45359237);
    }

    public static FactorConverter speed() {
        return new FactorConverter("Speed")
                .add("m/s", "Metre/second (m/s)", 1.0)
                .add("km/h", "Kilometre/hour (km/h)", 1000.0 / 3600.0)
                .add("mph", "Mile/hour (mph)", 1609.344 / 3600.0)
                .add("kn", "Knot (kn)", 1852.0 / 3600.0);
    }

    public static FactorConverter time() {
        return new FactorConverter("Time")
                .add("s", "Second (s)", 1.0)
                .add("min", "Minute (min)", 60.0)
                .add("h", "Hour (h)", 3600.0)
                .add("d", "Day (d)", 86400.0)
                .add("wk", "Week (wk)", 604800.0);
    }
}
