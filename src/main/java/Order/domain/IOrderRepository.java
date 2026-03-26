package Order.domain;

import Order.domain.models.Order;
import Order.infrastructure.exceptions.PersistenceException;

import java.util.Optional;
import java.util.UUID;

public interface IOrderRepository {
    Optional<Order> findById(UUID id);
    Order create(Order order);
    Order update(Order order) throws PersistenceException;
    Order save(Order order);
    void delete(Order order);
}
