package MarketData.client;

import MarketData.client.dtos.MarketPriceUpdateMessage;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MarketDataStreamState {
    private final AtomicReference<MarketPriceUpdateMessage> latestPriceUpdate = new AtomicReference<>();
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public void markConnected() {
        connected.set(true);
    }

    public void markDisconnected() {
        connected.set(false);
    }

    public boolean isConnected() {
        return connected.get();
    }

    public void updateLatestPrice(MarketPriceUpdateMessage message) {
        latestPriceUpdate.set(message);
    }

    public Optional<MarketPriceUpdateMessage> getLatestPriceUpdate() {
        return Optional.ofNullable(latestPriceUpdate.get());
    }
}
