package shared.common.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;

@Component("externalApi")
public class ExternalApiHealthIndicator implements HealthIndicator {
    private final RestClient restClient;

    public ExternalApiHealthIndicator() {
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect()
                .build();

        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Health health() {
        boolean isApiUp = checkExternalApi();

        if (!isApiUp) {
            return Health.down()
                    .withDetail("error", "API NOT UP")
                    .withDetail("errorCode", 504)
                    .build();
        }

        return Health.up()
                .withDetail("message", "API is up and fully operational.")
                .build();
    }

    private boolean checkExternalApi() {
        try {
            restClient.get()
                    .uri(URI.create("http://localhost:8080/actuator/health"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            return false;
        }
    }
}
