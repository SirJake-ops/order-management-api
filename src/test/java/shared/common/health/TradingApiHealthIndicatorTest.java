package shared.common.health;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TradingApiHealthIndicatorTest {

    @Test
    void shouldReportUpWhenMarketDataHealthCheckSucceeds() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("http://market-data.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        TradingApiHealthIndicator indicator = new TradingApiHealthIndicator(restClientBuilder.build(), "/health");

        server.expect(requestTo("http://market-data.test/health"))
                .andRespond(withSuccess());

        Health health = indicator.health();

        Assertions.assertEquals(Status.UP, health.getStatus());
        Assertions.assertEquals("market-data", health.getDetails().get("service"));
        Assertions.assertEquals("/health", health.getDetails().get("endpoint"));
        server.verify();
    }

    @Test
    void shouldReportDownWhenMarketDataHealthCheckReturnsUnhealthyStatus() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("http://market-data.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        TradingApiHealthIndicator indicator = new TradingApiHealthIndicator(restClientBuilder.build(), "health");

        server.expect(requestTo("http://market-data.test/health"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        Health health = indicator.health();

        Assertions.assertEquals(Status.DOWN, health.getStatus());
        Assertions.assertEquals("market-data", health.getDetails().get("service"));
        Assertions.assertEquals("/health", health.getDetails().get("endpoint"));
        Assertions.assertEquals(503, health.getDetails().get("status"));
        server.verify();
    }

    @Test
    void shouldReportDownWhenMarketDataHealthCheckCannotConnect() {
        RestClient.Builder restClientBuilder = RestClient.builder()
                .baseUrl("http://market-data.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        TradingApiHealthIndicator indicator = new TradingApiHealthIndicator(restClientBuilder.build(), "/health");

        server.expect(requestTo("http://market-data.test/health"))
                .andRespond(withException(new IOException("connection refused")));

        Health health = indicator.health();

        Assertions.assertEquals(Status.DOWN, health.getStatus());
        Assertions.assertEquals("market-data", health.getDetails().get("service"));
        Assertions.assertEquals("/health", health.getDetails().get("endpoint"));
        Assertions.assertTrue(health.getDetails().get("error").toString().contains("connection refused"));
        server.verify();
    }
}
