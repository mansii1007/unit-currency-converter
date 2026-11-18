package converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * High-level currency conversion: loads offline rates, converts amounts,
 * and optionally refreshes rates from the internet.
 */
public class CurrencyConverter implements CategoryConverter {
    private final CurrencyRates rates;
    private final LiveRateClient liveClient;
    private final Path writableRatesFile;

    public CurrencyConverter(CurrencyRates rates, LiveRateClient liveClient, Path writableRatesFile) {
        this.rates = rates;
        this.liveClient = liveClient;
        this.writableRatesFile = writableRatesFile;
    }

    /**
     * Loads bundled offline rates, then overlays a previously saved live file if it exists.
     */
    public static CurrencyConverter loadDefault() {
        CurrencyRates rates = new CurrencyRates();
        Path bundled = findBundledRates();
        if (bundled != null) {
            try {
                rates.load(bundled);
            } catch (IOException ex) {
                // keep the built-in INR=1.0 so the app still opens
            }
        }
        Path writable = Paths.get("data", "currency_rates.txt");
        if (Files.exists(writable)) {
            try {
                rates.load(writable);
            } catch (IOException ignored) {
                // stay on bundled rates
            }
        }
        return new CurrencyConverter(rates, new LiveRateClient(), writable);
    }

    public static Path findBundledRates() {
        Path[] candidates = new Path[] {
                Paths.get("resources", "currency_rates.txt"),
                Paths.get("src", "resources", "currency_rates.txt"),
                Paths.get("out", "currency_rates.txt")
        };
        for (Path path : candidates) {
            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
    }

    public CurrencyRates getRates() {
        return rates;
    }

    public RefreshResult refreshLiveRates() {
        RefreshResult result = liveClient.refresh(rates);
        if (result.isSuccess() && writableRatesFile != null) {
            try {
                rates.save(writableRatesFile);
            } catch (IOException ex) {
                return RefreshResult.fail(
                        "Rates were updated but could not be saved to disk: " + ex.getMessage());
            }
        }
        return result;
    }

    public String currencyLabel(String code) {
        switch (code.toUpperCase()) {
            case "INR": return "Indian Rupee (INR)";
            case "USD": return "US Dollar (USD)";
            case "EUR": return "Euro (EUR)";
            case "GBP": return "British Pound (GBP)";
            case "JPY": return "Japanese Yen (JPY)";
            case "AUD": return "Australian Dollar (AUD)";
            case "CAD": return "Canadian Dollar (CAD)";
            case "SGD": return "Singapore Dollar (SGD)";
            case "AED": return "UAE Dirham (AED)";
            case "CNY": return "Chinese Yuan (CNY)";
            case "CHF": return "Swiss Franc (CHF)";
            case "NZD": return "New Zealand Dollar (NZD)";
            default: return code;
        }
    }

    @Override
    public String getName() {
        return "Currency";
    }

    @Override
    public List<String> getUnitCodes() {
        List<String> codes = new ArrayList<>();
        for (String code : CurrencyRates.SUPPORTED) {
            if (rates.hasRate(code)) {
                codes.add(code);
            }
        }
        if (codes.isEmpty()) {
            codes.add("INR");
        }
        return codes;
    }

    @Override
    public String getLabel(String unitCode) {
        return currencyLabel(unitCode);
    }

    @Override
    public double convert(double value, String fromCode, String toCode) {
        return rates.convert(value, fromCode, toCode);
    }
}
