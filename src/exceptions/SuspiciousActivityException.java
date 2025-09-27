package exceptions;

public class SuspiciousActivityException extends RuntimeException {
    private String activityType;
    private String details;

    public SuspiciousActivityException(String activityType, String details) {
        super(String.format("Suspicious activity detected: %s - %s", activityType, details));
        this.activityType = activityType;
        this.details = details;
    }

    public SuspiciousActivityException(String activityType) {
        this(activityType, "Transaction flagged for manual review");
    }

    public String getActivityType() {
        return activityType;
    }

    public String getDetails() {
        return details;
    }
}
