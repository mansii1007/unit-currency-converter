package converter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Checks the conversion maths, input validation and rate-file loading.
 * Run with: java -cp out converter.ConversionLogicTest
 */
public class ConversionLogicTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        testLength();
        testWeight();
        testTemperature();
        testSpeedAndTime();
        testCurrency();
        testValidation();
        testJsonAndRatesFile();
        System.out.println();
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testLength() {
        CategoryConverter length = FactorConverter.length();
        check("100 cm = 1 m", almost(length.convert(100, "cm", "m"), 1.0));
        check("1 m = 100 cm", almost(length.convert(1, "m", "cm"), 100.0));
        check("1 inch = 2.54 cm", almost(length.convert(1, "in", "cm"), 2.54));
        check("1 mile = 1.609344 km", almost(length.convert(1, "mi", "km"), 1.609344));
        check("same unit leaves value unchanged", almost(length.convert(42, "m", "m"), 42.0));
    }

    private static void testWeight() {
        CategoryConverter weight = FactorConverter.weight();
        check("1 kg = 1000 g", almost(weight.convert(1, "kg", "g"), 1000.0));
        check("500 g = 0.5 kg", almost(weight.convert(500, "g", "kg"), 0.5));
        check("1 lb is about 0.453592 kg", almost(weight.convert(1, "lb", "kg"), 0.45359237));
    }

    private static void testTemperature() {
        CategoryConverter temp = new TemperatureConverter();
        check("32 F = 0 C", almost(temp.convert(32, "F", "C"), 0.0));
        check("0 C = 32 F", almost(temp.convert(0, "C", "F"), 32.0));
        check("0 C = 273.15 K", almost(temp.convert(0, "C", "K"), 273.15));
        check("273.15 K = 0 C", almost(temp.convert(273.15, "K", "C"), 0.0));
        check("100 C = 212 F", almost(temp.convert(100, "C", "F"), 212.0));
        check("-40 C = -40 F", almost(temp.convert(-40, "C", "F"), -40.0));
    }

    private static void testSpeedAndTime() {
        CategoryConverter speed = FactorConverter.speed();
        CategoryConverter time = FactorConverter.time();
        check("100 km/h = 27.777... m/s", almost(speed.convert(100, "km/h", "m/s"), 1000.0 / 36.0));
        check("1 hour = 60 minutes", almost(time.convert(1, "h", "min"), 60.0));
        check("90 minutes = 1.5 hours", almost(time.convert(90, "min", "h"), 1.5));
        check("2 days = 48 hours", almost(time.convert(2, "d", "h"), 48.0));
    }

    private static void testCurrency() throws Exception {
        CurrencyRates rates = new CurrencyRates();
        Path bundled = CurrencyConverter.findBundledRates();
        check("bundled rates file is present", bundled != null && Files.exists(bundled));
        rates.load(bundled);
        check("INR to INR stays the same", almost(rates.convert(100, "INR", "INR"), 100.0));
        double usd = rates.getRate("USD");
        check("100 INR to USD uses the file rate", almost(rates.convert(100, "INR", "USD"), 100 * usd));
        // Inverse should get back close to the original amount
        double back = rates.convert(rates.convert(250, "INR", "EUR"), "EUR", "INR");
        check("INR -> EUR -> INR returns 250", almost(back, 250.0));
        CurrencyConverter converter = new CurrencyConverter(rates, new LiveRateClient(), null);
        check("currency converter lists INR", converter.getUnitCodes().contains("INR"));
        check("currency converter lists USD", converter.getUnitCodes().contains("USD"));
    }

    private static void testValidation() {
        check("parses a plain number", almost(InputValidator.parseNonNegative("12.5"), 12.5));
        check("parses a number with commas", almost(InputValidator.parseNonNegative("1,000"), 1000.0));
        check("rejects an empty value", throwsMessage(() -> InputValidator.parseNumber("  "), "enter a value"));
        check("rejects text", throwsMessage(() -> InputValidator.parseNumber("abc"), "not a valid number"));
        check("rejects a negative weight/currency", throwsMessage(() -> InputValidator.parseNonNegative("-3"), "zero or greater"));
        check("allows a negative temperature", almost(InputValidator.parseTemperature("-10"), -10.0));
    }

    private static void testJsonAndRatesFile() throws Exception {
        String frankfurter = "{\"amount\":1.0,\"base\":\"INR\",\"date\":\"2026-11-18\",\"rates\":{\"USD\":0.0115,\"EUR\":0.0106}}";
        Map<String, Double> map = SimpleJson.extractNumberMap(frankfurter, "rates");
        check("JSON parser reads USD", almost(map.getOrDefault("USD", 0.0), 0.0115));
        check("JSON parser reads the date", "2026-11-18".equals(SimpleJson.extractString(frankfurter, "date")));

        CurrencyRates rates = new CurrencyRates();
        rates.setRate("USD", 0.01);
        int n = rates.mergeLiveRates(map, "frankfurter.app", "2026-11-18");
        check("merge applies live rates", n == 2 && almost(rates.getRate("USD"), 0.0115));
        check("status mentions live source", rates.statusText().contains("frankfurter.app"));

        Path temp = Files.createTempFile("rates", ".txt");
        try {
            rates.save(temp);
            CurrencyRates reloaded = new CurrencyRates();
            reloaded.load(temp);
            check("saved rates reload correctly", almost(reloaded.getRate("USD"), 0.0115));
            String saved = Files.readString(temp, StandardCharsets.UTF_8);
            check("saved file records the source", saved.contains("source=frankfurter.app"));
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static boolean almost(double actual, double expected) {
        return Math.abs(actual - expected) < 1e-9;
    }

    private static boolean throwsMessage(Throwing action, String snippet) {
        try {
            action.run();
            return false;
        } catch (IllegalArgumentException ex) {
            return ex.getMessage() != null && ex.getMessage().toLowerCase().contains(snippet.toLowerCase());
        } catch (Exception ex) {
            return false;
        }
    }

    @FunctionalInterface
    private interface Throwing {
        void run();
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("  PASS  " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }
}
