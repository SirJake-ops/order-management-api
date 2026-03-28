package MarketData.client.dtos;

public record MarketPriceUpdateMessage(
        String type,
        double price
) {
}
