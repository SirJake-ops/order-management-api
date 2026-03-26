package Order.application.services;


import Order.events.event.OrderCancelledEvent;
import Order.events.event.OrderMatchedEvent;
import Order.events.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Map;


@Slf4j
@Service
public class NotificationService {
    private final SimpMessagingTemplate messageTemplate;

    public NotificationService(ObjectProvider<SimpMessagingTemplate> messageTemplateProvider) {
        this.messageTemplate = messageTemplateProvider.getIfAvailable();
    }

    @EventListener
    public void handleOrderMatched(OrderMatchedEvent event) {
        if (messageTemplate == null) {
            log.debug("Skipping trade notification because SimpMessagingTemplate is not configured");
            return;
        }

        String symbol = event.getBuyOrder().getSymbol();

        messageTemplate.convertAndSendToUser("user", "/topic/trades" + symbol, Map.of(
                "type", "TRADE_EXECUTED",
                "symbol", symbol,
                "price", event.getMatchedPrice(),
                "quantity", event.getMatchedQuantity(),
                "timestamp", Instant.now()));

        log.info("Sent trade notification for symbol: {}", symbol);
    }

    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        if (messageTemplate == null) {
            log.debug("Skipping order placed notification because SimpMessagingTemplate is not configured");
            return;
        }

        messageTemplate.convertAndSendToUser("user", "/topic/trades", Map.of(
                "type", "ORDER_PLACED",
                "order", event.getOrder(),
                "timestamp", Instant.now()
        ));
    }


    @EventListener
    public void handleOrderCancelled(OrderCancelledEvent event) {
        if (messageTemplate == null) {
            log.debug("Skipping order cancelled notification because SimpMessagingTemplate is not configured");
            return;
        }

        messageTemplate.convertAndSendToUser("user", "/topic/trades", Map.of(
                "type", "ORDER_CANCELLED",
                "orderId", event.getOrder().getId(),
                "timestamp", Instant.now()
        ));
    }
}
