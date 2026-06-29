package shared.common.health;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

@Component("marketData")
public class TradingApiHealthIndicator
        implements HealthIndicator {
    private final RestClient restClient;
    private final String healthPath;

    public TradingApiHealthIndicator(@Qualifier("marketDataRestClient") RestClient restClient,
                                     @Value("${market-data.health-path:/health}") String healthPath) {
        this.restClient = restClient;
        this.healthPath = normalizeHealthPath(healthPath);
    }

    @Override
    public Health health() {
        try {
            restClient.get()
                    .uri(healthPath)
                    .retrieve()
                    .toBodilessEntity();

            return Health.up()
                    .withDetail("service", "market-data")
                    .withDetail("endpoint", healthPath)
                    .build();
        } catch (RestClientResponseException exception) {
            return Health.down()
                    .withDetail("service", "market-data")
                    .withDetail("endpoint", healthPath)
                    .withDetail("status", exception.getStatusCode().value())
                    .withDetail("error", exception.getStatusText())
                    .build();
        } catch (RestClientException exception) {
            return Health.down()
                    .withDetail("service", "market-data")
                    .withDetail("endpoint", healthPath)
                    .withDetail("error", exception.getMessage())
                    .build();
        }
    }

    private String normalizeHealthPath(String healthPath) {
        if (healthPath == null || healthPath.isBlank()) {
            return "/health";
        }

        return healthPath.startsWith("/") ? healthPath : "/" + healthPath;
    }
}
