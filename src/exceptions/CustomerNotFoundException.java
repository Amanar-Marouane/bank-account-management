package exceptions;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(String field, String value) {
        super("Customer not found with " + field + ": " + value);
    }
}
