package Order.application.exceptions;

public class InsufficientBalanceException extends OrderException {
    public InsufficientBalanceException(String userId) {
        super("Insufficient account balance for user: " + userId);
    }
}
