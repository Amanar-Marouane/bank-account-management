package exceptions;

public class NegativeAmountException extends RuntimeException {
    public NegativeAmountException(String message) {
        super(message);
    }

    public NegativeAmountException(double amount) {
        super("Amount cannot be negative or zero: " + amount);
    }
}
