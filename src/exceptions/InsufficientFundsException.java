package exceptions;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(double required, double available) {
        super("Insufficient funds: Required $" + String.format("%.2f", required) +
                ", Available $" + String.format("%.2f", available));
    }
}
