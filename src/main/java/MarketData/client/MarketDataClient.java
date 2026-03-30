package MarketData.client;

import MarketData.client.dtos.MarketPriceResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class MarketDataClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MarketDataClient(@Qualifier("marketDataRestClient") RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public Optional<MarketPriceResponse> getLatestPrice(String symbol) {
        try {
            return restClient.get()
                    .uri("/api/market/prices/{symbol}", symbol)
                    .exchange((request, response) -> {
                        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                            return Optional.<MarketPriceResponse>empty();
                        }

                        if (response.getStatusCode().is2xxSuccessful()) {
                            final String body = response.bodyTo(String.class);
                            if (body == null || body.isBlank()) {
                                return Optional.empty();
                            }

                            try {
                                return Optional.of(objectMapper.readValue(body, MarketPriceResponse.class));
                            } catch (JsonProcessingException exception) {
                                throw new RestClientException("Failed to parse market data response", exception);
                            }
                        }

                        throw new RestClientException("Market data request failed with status " + response.getStatusCode().value());
                    });
        } catch (RestClientException exception) {
            throw exception;
        }
    }
}
