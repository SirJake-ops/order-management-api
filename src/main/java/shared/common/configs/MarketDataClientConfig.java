package shared.common.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MarketDataClientConfig {

    @Bean("marketDataRestClient")
    public RestClient marketDataRestClient(@Value("${market-data.base-url:http://localhost:8080}") String marketDataBaseUrl) {
        return RestClient.builder()
                .baseUrl(marketDataBaseUrl)
                .build();
    }
}
