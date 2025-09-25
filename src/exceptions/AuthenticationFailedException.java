package exceptions;

public class AuthenticationFailedException extends RuntimeException {
    private String attemptType;
    private String reason;

    public AuthenticationFailedException(String attemptType, String reason) {
        super(String.format("Authentication failed during %s: %s", attemptType, reason));
        this.attemptType = attemptType;
        this.reason = reason;
    }

    public AuthenticationFailedException(String reason) {
        this("login", reason);
    }

    public String getAttemptType() {
        return attemptType;
    }

    public String getReason() {
        return reason;
    }
}
