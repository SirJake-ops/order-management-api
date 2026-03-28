package Order.infrastructure.exceptions;

public class PersistenceException extends RuntimeException {
    public PersistenceException(String message) {
       super(message);
    }
}
