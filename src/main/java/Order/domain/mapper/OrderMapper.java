package Order.domain.mapper;

import Order.domain.dtos.OrderDto;
import Order.domain.models.Order;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface OrderMapper {
    OrderDto toDto(Order order);
    
    @Mapping(target = "deletedDate", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "id", ignore = true)
    Order toEntity(OrderDto orderDto);

    List<OrderDto> toDtos(List<Order> orders);


    List<Order> toEntities(List<OrderDto> orderDtos);
}
