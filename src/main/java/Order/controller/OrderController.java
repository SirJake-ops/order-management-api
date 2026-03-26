package Order.controller;

import Order.application.services.OrderService;
import Order.domain.dtos.LimitOrderDto;
import Order.domain.dtos.OrderDto;
import Order.domain.mapper.OrderMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    public OrderController(OrderMapper orderMapper, OrderService orderService) {
        this.orderMapper = orderMapper;
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto) {
        OrderDto savedOrder = orderService.createOrder(orderDto);
        return ResponseEntity.ok(savedOrder);
    }

    @PostMapping("/market")
    public ResponseEntity<OrderDto> createMarketOrder(@Valid @RequestBody OrderDto orderDto) {
        /*
          This is a reminder for me, that this method is not the same as the method above.
          This is creating a market order that then also will execute the order, and not just have
          it persist in the db for later.
          */
        return ResponseEntity.ok(orderDto);
    }

    @PostMapping("/limit")
    public ResponseEntity<LimitOrderDto> createLimitOrder(@Valid @RequestBody LimitOrderDto limitOrderDto) {
        /*
        This similar to the above method is a placeholder for a future method. The logic to execute an
        order is not setup yet (Have not setup WS or real-time comms), so this method only returns its dto
         */
        return ResponseEntity.ok(limitOrderDto);
    }

    @PutMapping("/{id}/modify")
    public ResponseEntity<OrderDto> updateOrder(@Valid @RequestBody OrderDto orderDto, @PathVariable UUID id) {
        OrderDto updatedOrder = orderService.updateOrder(orderDto, id);
        return ResponseEntity.ok(updatedOrder);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable UUID id) {
        OrderDto canceledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(canceledOrder);
    }
}
