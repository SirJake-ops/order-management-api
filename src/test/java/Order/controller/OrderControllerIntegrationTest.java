package Order.controller;

import Order.domain.dtos.OrderDto;
import Order.enums.OrderStatus;
import Order.enums.OrderType;
import Order.enums.Side;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.math.BigDecimal;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ComponentScan(basePackages = {"org.example.taskmanagement", "Order", "shared"},
               excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                                                     classes = {shared.common.configs.WebSecurityConfig.class}))
@EnableJpaRepositories(basePackages = {"Order.infrastructure.persistence"})
@EntityScan(basePackages = {"Order.domain.models", "shared.common.entities"})
@SpringBootTest(classes = org.example.taskmanagement.TradingPlatformApplication.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("Should return a Ok response when creating a valid order")
    public void shouldReturnOk_WhenCreatingOrder() throws Exception {
        OrderDto orderDto = OrderDto.builder()
                .price(new BigDecimal("50000.50"))
                .quantity(new BigDecimal("0.001"))
                .symbol("BTC/USD")
                .side(Side.BUY)
                .orderType(OrderType.LIMIT)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return a BadRequest response when creating an order with invalid data")
    public void shouldReturnBadRequest_WhenCreatingOrderWithInvalidData() throws Exception {
        OrderDto orderDto = OrderDto.builder()
                .price(new BigDecimal("-50000.50"))
                .quantity(new BigDecimal("0.001"))
                .symbol("BTC/USD")
                .side(Side.BUY)
                .orderType(OrderType.LIMIT)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return an Ok response when updateing an existing order")
    public void shouldReturnOk_WhenUpdatingExistingOrder() throws Exception {
        OrderDto orderDto = OrderDto.builder()
                .price(new BigDecimal("50000.50"))
                .quantity(new BigDecimal("0.001"))
                .symbol("BTC/USD")
                .side(Side.BUY)
                .orderType(OrderType.LIMIT)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        String response = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDto createdOrder = objectMapper.readValue(response, OrderDto.class);

        OrderDto updatedOrderDto = OrderDto.builder()
                .price(new BigDecimal("51000.75"))
                .quantity(new BigDecimal("0.002"))
                .symbol("BTC/USD")
                .side(Side.BUY)
                .orderType(OrderType.LIMIT)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/orders/" + createdOrder.getId() + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOrderDto)))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.price").value("51000.75"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.quantity").value("0.002"));
    }

    @Test
    @DisplayName("Should return a BadRequest response when updating a non-existing order")
    public void shouldReturnBadRequest_WhenUpdatingNonExistingOrder() throws Exception {
        OrderDto updatedOrderDto = OrderDto.builder()
                .price(new BigDecimal("51000.75"))
                .quantity(new BigDecimal("0.002"))
                .symbol("BTC/USD")
                .side(Side.SELL)
                .orderType(OrderType.LIMIT)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/orders/" + "00000000-0000-0000-0000-000000000000" + "/modify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOrderDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should return an Ok response when canceling an existing order")
    public void shouldReturnOk_WhenCancelingExistingOrder() throws Exception {
        OrderDto orderDto = OrderDto.builder()
                .price(new BigDecimal("50000.50"))
                .quantity(new BigDecimal("0.001"))
                .symbol("BTC/USD")
                .side(Side.BUY)
                .orderType(OrderType.LIMIT)
                .filledQuantity(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .build();

        String response = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDto createdOrder = objectMapper.readValue(response, OrderDto.class);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/orders/" + createdOrder.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("Should return a BadRequest response when canceling a non-existing order")
    public void shouldReturnBadRequest_WhenCancelingNonExistingOrder() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/orders/" + "00000000-0000-0000-0000-000000000000" + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
