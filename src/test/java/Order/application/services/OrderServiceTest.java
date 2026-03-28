package Order.application.services;

import MarketData.client.MarketDataClient;
import MarketData.client.dtos.MarketPriceResponse;
import Order.application.exceptions.MarketDataUnavailableException;
import Order.application.exceptions.MarketPriceNotFoundException;
import Order.application.exceptions.OrderException;
import Order.domain.IOrderRepository;
import Order.domain.dtos.OrderDto;
import Order.domain.mapper.OrderMapper;
import Order.domain.models.Order;
import Order.enums.OrderStatus;
import Order.enums.OrderType;
import Order.enums.Side;
import Order.events.event.OrderPlacedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private MarketDataClient marketDataClient;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderMapper, orderRepository, applicationEventPublisher, marketDataClient);
    }

    @Test
    @DisplayName("Should enrich a market order with market data before saving")
    public void shouldEnrichMarketOrderWithMarketDataBeforeSaving() {
        OrderDto orderDto = OrderDto.builder()
                .quantity(new BigDecimal("1.50"))
                .symbol("BTC/USD")
                .side(Side.BUY)
                .orderType(OrderType.MARKET)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        Order order = Order.builder()
                .quantity(new BigDecimal("1.50"))
                .symbol("BTC/USD")
                .side(Side.BUY)
                .orderType(OrderType.MARKET)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        OrderDto savedOrderDto = OrderDto.builder()
                .price(new BigDecimal("62150.25"))
                .quantity(new BigDecimal("1.50"))
                .symbol("BTC/USD")
                .side(Side.BUY)
                .orderType(OrderType.MARKET)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        Mockito.when(orderMapper.toEntity(orderDto)).thenReturn(order);
        Mockito.when(marketDataClient.getLatestPrice("BTC/USD"))
                .thenReturn(Optional.of(new MarketPriceResponse("BTC/USD", 62149.10, 62151.35, 62150.25, 5000L, 123456789L)));
        Mockito.when(orderRepository.save(order)).thenReturn(order);
        Mockito.when(orderMapper.toDto(order)).thenReturn(savedOrderDto);

        OrderDto result = orderService.createOrder(orderDto);

        Assertions.assertEquals(savedOrderDto, result);
        Assertions.assertEquals(BigDecimal.valueOf(62150.25), order.getPrice());

        ArgumentCaptor<OrderPlacedEvent> orderPlacedEventArgumentCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        Mockito.verify(orderRepository).save(order);
        Mockito.verify(applicationEventPublisher).publishEvent(orderPlacedEventArgumentCaptor.capture());
        Assertions.assertEquals(order, orderPlacedEventArgumentCaptor.getValue().getOrder());
    }

    @Test
    @DisplayName("Should create a limit order without calling the market data client")
    public void shouldCreateLimitOrderWithoutCallingTheMarketDataClient() {
        OrderDto orderDto = OrderDto.builder()
                .price(new BigDecimal("51000.75"))
                .quantity(new BigDecimal("0.50"))
                .symbol("BTC/USD")
                .side(Side.SELL)
                .orderType(OrderType.LIMIT)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        Order order = Order.builder()
                .price(new BigDecimal("51000.75"))
                .quantity(new BigDecimal("0.50"))
                .symbol("BTC/USD")
                .side(Side.SELL)
                .orderType(OrderType.LIMIT)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        Mockito.when(orderMapper.toEntity(orderDto)).thenReturn(order);
        Mockito.when(orderRepository.save(order)).thenReturn(order);
        Mockito.when(orderMapper.toDto(order)).thenReturn(orderDto);

        OrderDto result = orderService.createOrder(orderDto);

        Assertions.assertEquals(orderDto, result);
        Mockito.verifyNoInteractions(marketDataClient);
        Mockito.verify(orderRepository).save(order);
        Mockito.verify(applicationEventPublisher).publishEvent(Mockito.any(OrderPlacedEvent.class));
    }

    @Test
    @DisplayName("Should throw an exception when market data is missing for a market order")
    public void shouldThrowException_WhenMarketDataIsMissingForMarketOrder() {
        OrderDto orderDto = OrderDto.builder()
                .quantity(new BigDecimal("2.00"))
                .symbol("ETH/USD")
                .side(Side.BUY)
                .orderType(OrderType.MARKET)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        Order order = Order.builder()
                .quantity(new BigDecimal("2.00"))
                .symbol("ETH/USD")
                .side(Side.BUY)
                .orderType(OrderType.MARKET)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        Mockito.when(orderMapper.toEntity(orderDto)).thenReturn(order);
        Mockito.when(marketDataClient.getLatestPrice("ETH/USD")).thenReturn(Optional.empty());

        MarketPriceNotFoundException exception = Assertions.assertThrows(MarketPriceNotFoundException.class, () -> orderService.createOrder(orderDto));

        Assertions.assertEquals("No market data available for symbol: ETH/USD", exception.getMessage());
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any(Order.class));
        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(Mockito.any(OrderPlacedEvent.class));
    }

    @Test
    @DisplayName("Should throw an exception when the market data request fails")
    public void shouldThrowException_WhenTheMarketDataRequestFails() {
        OrderDto orderDto = OrderDto.builder()
                .quantity(new BigDecimal("3.00"))
                .symbol("SOL/USD")
                .side(Side.BUY)
                .orderType(OrderType.MARKET)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        Order order = Order.builder()
                .quantity(new BigDecimal("3.00"))
                .symbol("SOL/USD")
                .side(Side.BUY)
                .orderType(OrderType.MARKET)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        Mockito.when(orderMapper.toEntity(orderDto)).thenReturn(order);
        Mockito.when(marketDataClient.getLatestPrice("SOL/USD")).thenThrow(new RestClientException("boom"));

        MarketDataUnavailableException exception = Assertions.assertThrows(MarketDataUnavailableException.class, () -> orderService.createOrder(orderDto));

        Assertions.assertEquals("Unable to retrieve market data for symbol: SOL/USD", exception.getMessage());
        Mockito.verify(orderRepository, Mockito.never()).save(Mockito.any(Order.class));
        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(Mockito.any(OrderPlacedEvent.class));
    }
}
