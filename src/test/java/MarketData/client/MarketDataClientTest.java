package MarketData.client;

import MarketData.client.dtos.MarketPriceResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MarketDataClientTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private HttpRequest request;

    @Mock
    private RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response;

    private MarketDataClient marketDataClient;

    @BeforeEach
    void setUp() {
        marketDataClient = new MarketDataClient(restClient);
    }

    @Test
    @DisplayName("Should return latest market price when request is successful")
    public void shouldReturnLatestMarketPrice_WhenRequestIsSuccessful() throws IOException {
        MarketPriceResponse marketPriceResponse = new MarketPriceResponse("BTC/USD", 62149.10, 62151.35, 62150.25, 5000L, 123456789L);

        mockGetRequest("BTC/USD");
        Mockito.when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        Mockito.when(response.bodyTo(MarketPriceResponse.class)).thenReturn(marketPriceResponse);
        Mockito.when(requestHeadersSpec.exchange(Mockito.<RestClient.RequestHeadersSpec.ExchangeFunction<Optional<MarketPriceResponse>>>any()))
                .thenAnswer(invocation -> invocation.<RestClient.RequestHeadersSpec.ExchangeFunction<Optional<MarketPriceResponse>>>getArgument(0).exchange(request, response));

        Optional<MarketPriceResponse> result = marketDataClient.getLatestPrice("BTC/USD");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(marketPriceResponse, result.get());
    }

    @Test
    @DisplayName("Should return empty when market price is not found")
    public void shouldReturnEmpty_WhenMarketPriceIsNotFound() throws IOException {
        mockGetRequest("ETH/USD");
        Mockito.when(response.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        Mockito.when(requestHeadersSpec.exchange(Mockito.<RestClient.RequestHeadersSpec.ExchangeFunction<Optional<MarketPriceResponse>>>any()))
                .thenAnswer(invocation -> invocation.<RestClient.RequestHeadersSpec.ExchangeFunction<Optional<MarketPriceResponse>>>getArgument(0).exchange(request, response));

        Optional<MarketPriceResponse> result = marketDataClient.getLatestPrice("ETH/USD");

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should throw an exception when market data service returns an unexpected status")
    public void shouldThrowException_WhenMarketDataServiceReturnsUnexpectedStatus() throws IOException {
        mockGetRequest("SOL/USD");
        Mockito.when(response.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        Mockito.when(requestHeadersSpec.exchange(Mockito.<RestClient.RequestHeadersSpec.ExchangeFunction<Optional<MarketPriceResponse>>>any()))
                .thenAnswer(invocation -> invocation.<RestClient.RequestHeadersSpec.ExchangeFunction<Optional<MarketPriceResponse>>>getArgument(0).exchange(request, response));

        RestClientException exception = Assertions.assertThrows(RestClientException.class, () -> marketDataClient.getLatestPrice("SOL/USD"));

        Assertions.assertEquals("Market data request failed with status 500", exception.getMessage());
    }

    private void mockGetRequest(String symbol) {
        Mockito.when(restClient.get()).thenReturn(requestHeadersUriSpec);
        Mockito.when(requestHeadersUriSpec.uri("/api/market/prices/{symbol}", symbol)).thenReturn(requestHeadersSpec);
    }
}
