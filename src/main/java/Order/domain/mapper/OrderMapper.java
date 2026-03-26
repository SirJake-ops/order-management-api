package Order.domain.mapper;

import Order.domain.dtos.OrderDto;
import Order.domain.models.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDto toDto(Order order);
    
    @Mapping(target = "deleted", ignore = true)
    Order toEntity(OrderDto orderDto);

    List<OrderDto> toDtos(List<Order> orders);


    List<Order> toEntities(List<OrderDto> orderDtos);
}
