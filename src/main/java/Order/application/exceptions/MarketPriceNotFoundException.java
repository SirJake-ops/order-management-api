package Order.application.exceptions;

public class MarketPriceNotFoundException extends OrderException {
    public MarketPriceNotFoundException(String symbol) {
        super("No market data available for symbol: " + symbol);
    }
}
