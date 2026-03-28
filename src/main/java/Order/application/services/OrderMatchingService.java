package Order.application.services;

import Order.application.exceptions.OrderException;
import Order.domain.IOrderRepository;
import Order.domain.models.Order;
import Order.enums.OrderStatus;
import Order.enums.OrderType;
import Order.enums.Side;
import Order.events.event.OrderMatchedEvent;
import Order.events.event.OrderPlacedEvent;
import Order.events.event.OrderRejectEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
public class OrderMatchingService {
    private final OrderBookService orderBookService;
    private final IOrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserBalanceService userBalanceService;

    public OrderMatchingService(OrderBookService orderBookService, IOrderRepository orderRepository, ApplicationEventPublisher eventPublisher, UserBalanceService userBalanceService) {
        this.orderBookService = orderBookService;
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.userBalanceService = userBalanceService;
    }

    @EventListener
    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {
        Order order = event.getOrder();

        try {
            if (order.getOrderType() == OrderType.MARKET) {
                executeMarketOrder(order);
            } else {
                executeMarketLimitOrder(order);
            }
        } catch(Exception ex) {
            log.error("Order processing failed: {}", order.getId(), ex);
            
            order.setStatus(OrderStatus.REJECTED);
            orderRepository.save(order);
            eventPublisher.publishEvent(new OrderRejectEvent(this, order, ex.getMessage()));
        }
    }


    private void executeMarketOrder(Order order) {
        Optional<Order> matchOpt = orderBookService.findBestMatch(order);

        if (matchOpt.isEmpty()) {
            order.setStatus(OrderStatus.REJECTED);
            orderRepository.save(order);

            eventPublisher.publishEvent(new OrderRejectEvent(this, order, "No matching order found."));
            return;
        }

        Order matchingOrder = matchOpt.get();

        executeTrade(order, matchingOrder);
    }


    private void executeMarketLimitOrder(Order order) {
        Optional<Order> matchOpt = orderBookService.findBestMatch(order);

        if (matchOpt.isPresent()) {
            Order matchingOrder = matchOpt.get();
            executeTrade(order, matchingOrder);
        } else {
            order.setStatus(OrderStatus.PENDING);
            orderRepository.save(order);
            orderBookService.addOrder(order);
            log.info("Order has been added to order book: {}", order.getId());
        }
    }

    private void executeTrade(Order incomingOrder, Order bookOrder) {
        BigDecimal tradePrice = incomingOrder.getPrice();
        BigDecimal tradeQuantity = incomingOrder.getQuantity().min(bookOrder.getRemainingQuantity());

        updateOrderAfterTrade(incomingOrder, tradeQuantity);
        updateOrderAfterTrade(bookOrder, tradeQuantity);

        orderRepository.save(incomingOrder);
        orderRepository.save(bookOrder);

        if (bookOrder.isFullyFilled()) {
            orderBookService.removeOrder(incomingOrder);
        }

        userBalanceService.processTradeSettlement(incomingOrder, bookOrder, tradePrice, tradeQuantity);

        Order buyOrder = incomingOrder.getSide() == Side.BUY ? incomingOrder : bookOrder;
        Order sellOrder = incomingOrder.getSide() == Side.SELL ? incomingOrder : bookOrder;

        eventPublisher.publishEvent(new OrderMatchedEvent(this, buyOrder, sellOrder, tradePrice, tradeQuantity));

        log.info("Executed trade: {} @ {} for {}", tradeQuantity, tradePrice, incomingOrder);
    }

    private void updateOrderAfterTrade(Order order, BigDecimal tradedQuantity) {
        BigDecimal newFilledQuantity = order.getFilledQuantity().add(tradedQuantity);
        order.setFilledQuantity(newFilledQuantity);

        if (order.isFullyFilled()) {
            order.setStatus(OrderStatus.FILLED);
        } else if (order.isPartiallyFilled()) {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
        }
    }
}
