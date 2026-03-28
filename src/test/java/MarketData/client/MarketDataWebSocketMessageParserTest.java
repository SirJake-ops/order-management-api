package MarketData.client;

import MarketData.client.dtos.MarketPriceUpdateMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class MarketDataWebSocketMessageParserTest {

    private final MarketDataWebSocketMessageParser parser =
            new MarketDataWebSocketMessageParser(new ObjectMapper());

    @Test
    @DisplayName("Should parse websocket price update payload")
    void shouldParsePriceUpdatePayload() {
        Optional<MarketPriceUpdateMessage> result =
                parser.parse("{\"type\":\"price_update\",\"price\":123.45}");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("price_update", result.get().type());
        Assertions.assertEquals(123.45, result.get().price());
    }

    @Test
    @DisplayName("Should ignore websocket payload with unsupported type")
    void shouldIgnoreUnsupportedPayloadType() {
        Optional<MarketPriceUpdateMessage> result =
                parser.parse("{\"type\":\"subscribed\"}");

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should ignore malformed websocket payload")
    void shouldIgnoreMalformedPayload() {
        Optional<MarketPriceUpdateMessage> result = parser.parse("not-json");

        Assertions.assertTrue(result.isEmpty());
    }
}
