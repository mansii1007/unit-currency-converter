package converter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Very small helper for reading a few fields from the exchange-rate JSON.
 * It is intentionally simple so the project does not need an extra library.
 */
public final class SimpleJson {
    private static final Pattern STRING_FIELD =
            Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern NUMBER_ENTRY =
            Pattern.compile("\"([A-Z]{3})\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)");

    private SimpleJson() {
    }

    public static String extractString(String json, String key) {
        Matcher matcher = STRING_FIELD.matcher(json);
        while (matcher.find()) {
            if (key.equals(matcher.group(1))) {
                return matcher.group(2);
            }
        }
        return null;
    }

    /**
     * Reads the numeric map inside a named object, e.g. the "rates" object.
     */
    public static Map<String, Double> extractNumberMap(String json, String objectKey) {
        Map<String, Double> result = new LinkedHashMap<>();
        int keyAt = indexOfKey(json, objectKey);
        if (keyAt < 0) {
            return result;
        }
        int brace = json.indexOf('{', keyAt);
        if (brace < 0) {
            return result;
        }
        int end = findMatchingBrace(json, brace);
        if (end < 0) {
            end = json.length();
        }
        String body = json.substring(brace, end + 1);
        Matcher matcher = NUMBER_ENTRY.matcher(body);
        while (matcher.find()) {
            result.put(matcher.group(1), Double.parseDouble(matcher.group(2)));
        }
        return result;
    }

    private static int indexOfKey(String json, String key) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:");
        Matcher m = p.matcher(json);
        return m.find() ? m.start() : -1;
    }

    private static int findMatchingBrace(String json, int openIndex) {
        int depth = 0;
        for (int i = openIndex; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}
