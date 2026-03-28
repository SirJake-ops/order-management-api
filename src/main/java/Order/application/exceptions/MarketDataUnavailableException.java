package Order.application.exceptions;

public class MarketDataUnavailableException extends OrderException {
    public MarketDataUnavailableException(String symbol) {
        super("Unable to retrieve market data for symbol: " + symbol);
    }
}
