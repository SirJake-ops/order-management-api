package org.example.taskmanagement;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"org.example.taskmanagement", "Order", "shared", "MarketData", "AuctionUser"})
@EnableJpaRepositories(basePackages = {"Order.infrastructure.persistence", "AuctionUser.infrastructure.persistence"})
@EntityScan(basePackages = {"Order.domain.models", "shared.common.entities", "AuctionUser.domain.models"})
public class TradingPlatformApplication {

    public static void main(String[] args) {
        Dotenv.configure()
                .ignoreIfMissing()
                .load();
        SpringApplication.run(TradingPlatformApplication.class, args);
    }

}
