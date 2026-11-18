package converter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Fetches INR-based rates from a public API that does not need an API key.
 * Tries Frankfurter first, then open.er-api.com, and reports a clear error
 * if both fail (no internet, timeout, unexpected response).
 */
public class LiveRateClient {
    public static final String FRANKFURTER = "https://api.frankfurter.app/latest?from=INR";
    public static final String OPEN_ER_API = "https://open.er-api.com/v6/latest/INR";

    private final HttpClient client;

    public LiveRateClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public RefreshResult refresh(CurrencyRates rates) {
        Exception lastError = null;
        try {
            String body = get(FRANKFURTER);
            Map<String, Double> map = SimpleJson.extractNumberMap(body, "rates");
            if (!map.isEmpty()) {
                // Frankfurter quotes other currencies per 1 INR when ?from=INR
                String date = SimpleJson.extractString(body, "date");
                int n = rates.mergeLiveRates(map, "frankfurter.app", date);
                if (n > 0) {
                    return RefreshResult.ok("frankfurter.app", n, rates.getLastUpdated());
                }
            }
        } catch (Exception ex) {
            lastError = ex;
        }

        try {
            String body = get(OPEN_ER_API);
            Map<String, Double> map = SimpleJson.extractNumberMap(body, "rates");
            if (!map.isEmpty()) {
                String date = SimpleJson.extractString(body, "time_last_update_utc");
                if (date == null) {
                    date = SimpleJson.extractString(body, "date");
                }
                int n = rates.mergeLiveRates(map, "open.er-api.com", date);
                if (n > 0) {
                    return RefreshResult.ok("open.er-api.com", n, rates.getLastUpdated());
                }
            }
        } catch (Exception ex) {
            lastError = ex;
        }

        String extra = lastError == null ? "" : " Last error: " + lastError.getMessage() + ".";
        return RefreshResult.fail(
                "Could not reach a live rate service. Offline rates will be used instead." + extra);
    }

    private String get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", "UnitCurrencyConverter/1.0 (student project)")
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode() + " from " + url);
        }
        String body = response.body();
        if (body == null || body.isBlank()) {
            throw new IllegalStateException("Empty response from " + url);
        }
        return body;
    }
}
