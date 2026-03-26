package MarketData.client.dtos;

public record MarketPriceResponse(
        String symbol,
        double bid,
        double ask,
        double last,
        long volume,
        long timestamp
) {
}
