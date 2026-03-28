package MarketData.client;

import MarketData.client.dtos.MarketPriceUpdateMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MarketDataWebSocketMessageParser {
    private final ObjectMapper objectMapper;

    public MarketDataWebSocketMessageParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<MarketPriceUpdateMessage> parse(String payload) {
        try {
            MarketDataEnvelope envelope = objectMapper.readValue(payload, MarketDataEnvelope.class);
            if (!"price_update".equals(envelope.type) || envelope.price == null) {
                return Optional.empty();
            }

            return Optional.of(new MarketPriceUpdateMessage(envelope.type, envelope.price));
        } catch (JsonProcessingException exception) {
            return Optional.empty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static final class MarketDataEnvelope {
        public String type;
        public Double price;
    }
}
