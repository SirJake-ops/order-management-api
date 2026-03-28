package Order.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LimitOrderDto {
    private UUID id;
    private BigDecimal price;
    private BigDecimal quantity;
}
