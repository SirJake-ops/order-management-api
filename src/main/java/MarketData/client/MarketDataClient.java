package MarketData.client;

import MarketData.client.dtos.MarketPriceResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class MarketDataClient {
    private final RestClient restClient;

    public MarketDataClient(@Qualifier("marketDataRestClient") RestClient restClient) {
        this.restClient = restClient;
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
                            return Optional.ofNullable(response.bodyTo(MarketPriceResponse.class));
                        }

                        throw new RestClientException("Market data request failed with status " + response.getStatusCode().value());
                    });
        } catch (RestClientException exception) {
            throw exception;
        }
    }
}
