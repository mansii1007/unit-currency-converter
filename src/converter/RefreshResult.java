package converter;

/** Outcome of a "Refresh live rates" attempt. */
public final class RefreshResult {
    private final boolean success;
    private final String message;
    private final String source;

    private RefreshResult(boolean success, String message, String source) {
        this.success = success;
        this.message = message;
        this.source = source;
    }

    public static RefreshResult ok(String source, int count, String updated) {
        return new RefreshResult(true,
                "Live rates updated from " + source + " (" + count + " currencies, as of " + updated + ").",
                source);
    }

    public static RefreshResult fail(String message) {
        return new RefreshResult(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }
}
