package Order.infrastructure.repos;

import Order.domain.IOrderRepository;
import Order.domain.models.Order;
import Order.infrastructure.exceptions.PersistenceException;
import Order.infrastructure.persistence.OrderRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class OrderRepositoryAdapter implements IOrderRepository {

    private final OrderRepository orderRepository;

    public OrderRepositoryAdapter(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order create(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Order update(Order order) throws PersistenceException {
        boolean isOrderCreated = orderRepository.findById(order.getId()).isEmpty();

        if (!isOrderCreated) {
            return orderRepository.save(order);
        } else {
            throw new PersistenceException("Order cannot be updated.");
        }
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public void delete(Order order) {
        order.softDelete();
    }
}
