package exceptions;

public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(String operation, String reason) {
        super("Invalid " + operation + " transaction: " + reason);
    }
}
