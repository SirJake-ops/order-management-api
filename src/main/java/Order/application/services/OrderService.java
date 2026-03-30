package Order.application.services;

import MarketData.client.MarketDataClient;
import MarketData.client.dtos.MarketPriceResponse;
import Order.application.exceptions.MarketDataUnavailableException;
import Order.application.exceptions.MarketPriceNotFoundException;
import Order.application.exceptions.OrderException;
import Order.application.exceptions.OrderNotFoundException;
import Order.domain.IOrderRepository;
import Order.domain.dtos.OrderDto;
import Order.domain.mapper.OrderMapper;
import Order.domain.models.Order;
import Order.enums.OrderStatus;
import Order.enums.OrderType;
import Order.enums.Side;
import Order.events.event.OrderCancelledEvent;
import Order.events.event.OrderPlacedEvent;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final IOrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MarketDataClient marketDataClient;

    public OrderService(OrderMapper orderMapper, IOrderRepository orderRepository, ApplicationEventPublisher applicationEventPublisher, MarketDataClient marketDataClient) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.marketDataClient = marketDataClient;
    }

    @Transactional
    public OrderDto createOrder(@Valid OrderDto orderDto) {
        Order order = orderMapper.toEntity(orderDto);
        initializeNewOrderDefaults(order);
        enrichOrderWithMarketData(order);
        orderRepository.save(order);
        applicationEventPublisher.publishEvent(new OrderPlacedEvent(this, order));
        return orderMapper.toDto(order);
    }

    public OrderDto updateOrder(@Valid OrderDto orderDto, UUID id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            order.get().setPrice(orderDto.getPrice() != null ? orderDto.getPrice() : BigDecimal.ZERO);
            order.get().setQuantity(orderDto.getQuantity() != null ? orderDto.getQuantity() : BigDecimal.ZERO);
            order.get().setSymbol(orderDto.getSymbol() != null ? orderDto.getSymbol() : "");
            order.get().setOrderType(orderDto.getOrderType() != null ? orderDto.getOrderType() : OrderType.MARKET);
            order.get().setSide(orderDto.getSide() != null ? orderDto.getSide() : Side.BUY);
            order.get().setFilledQuantity(orderDto.getFilledQuantity() != null ? orderDto.getFilledQuantity() : BigDecimal.ZERO);
            order.get().setStatus(orderDto.getStatus() != null ? orderDto.getStatus() : OrderStatus.PENDING);
            return orderMapper.toDto(orderRepository.save(order.get()));
        } else {
            throw new OrderNotFoundException(id.toString());
        }
    }

    @Transactional
    public OrderDto cancelOrder(UUID id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            order.get().setStatus(OrderStatus.CANCELLED);
            Order orderCancelled = orderRepository.save(order.get());
            applicationEventPublisher.publishEvent(new OrderCancelledEvent(this, orderCancelled));
            return  orderMapper.toDto(orderCancelled);
        } else {
            throw new OrderNotFoundException(id.toString());
        }
    }

    private void enrichOrderWithMarketData(Order order) {
        if (order.getSymbol() == null || order.getOrderType() != OrderType.MARKET) {
            return;
        }

        try {
            MarketPriceResponse priceResponse = marketDataClient.getLatestPrice(order.getSymbol())
                    .orElseThrow(() -> new MarketPriceNotFoundException(order.getSymbol()));

            order.setPrice(BigDecimal.valueOf(priceResponse.last()));
        } catch (RestClientException exception) {
            throw new MarketDataUnavailableException(order.getSymbol());
        }
    }

    private void initializeNewOrderDefaults(Order order) {
        if (order.getFilledQuantity() == null) {
            order.setFilledQuantity(BigDecimal.ZERO);
        }

        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PENDING);
        }
    }
}
