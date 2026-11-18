package converter;

import java.util.Arrays;
import java.util.List;

/**
 * Temperature conversion cannot use a simple factor because the scales
 * have different zero points. Everything is converted through Celsius.
 */
public class TemperatureConverter implements CategoryConverter {
    public static final String C = "C";
    public static final String F = "F";
    public static final String K = "K";

    @Override
    public String getName() {
        return "Temperature";
    }

    @Override
    public List<String> getUnitCodes() {
        return Arrays.asList(C, F, K);
    }

    @Override
    public String getLabel(String unitCode) {
        switch (unitCode) {
            case C:
                return "Celsius (°C)";
            case F:
                return "Fahrenheit (°F)";
            case K:
                return "Kelvin (K)";
            default:
                return unitCode;
        }
    }

    @Override
    public double convert(double value, String fromCode, String toCode) {
        double celsius = toCelsius(value, fromCode);
        return fromCelsius(celsius, toCode);
    }

    private double toCelsius(double value, String from) {
        switch (from) {
            case C:
                return value;
            case F:
                return (value - 32.0) * 5.0 / 9.0;
            case K:
                return value - 273.15;
            default:
                throw new IllegalArgumentException("Unknown temperature unit: " + from);
        }
    }

    private double fromCelsius(double celsius, String to) {
        switch (to) {
            case C:
                return celsius;
            case F:
                return celsius * 9.0 / 5.0 + 32.0;
            case K:
                return celsius + 273.15;
            default:
                throw new IllegalArgumentException("Unknown temperature unit: " + to);
        }
    }
}
