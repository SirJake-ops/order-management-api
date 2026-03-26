package Order.application.services;

import AuctionUser.domain.IApplicationUserRepository;
import AuctionUser.domain.exceptions.UserNotFoundException;
import AuctionUser.domain.models.TradingUser;
import Order.application.exceptions.InsufficientBalanceException;
import Order.domain.models.Order;
import Order.enums.Side;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class UserBalanceService {
    private final IApplicationUserRepository applicationUserRepository;

    public UserBalanceService(IApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }

    @Transactional
    void processTradeSettlement(Order incomingOrder, Order bookOrder, BigDecimal tradePrice, BigDecimal tradeQuantity) {
        BigDecimal totalAmount = tradeQuantity.multiply(tradePrice);

        TradingUser buyer = getUserForOrder(incomingOrder.getSide() == Side.BUY ? incomingOrder : bookOrder);
        if (buyer.getAccountBalance().compareTo(totalAmount) < 0) {
            throw new InsufficientBalanceException(buyer.getId().toString());
        }
        buyer.setAccountBalance(buyer.getAccountBalance().subtract(totalAmount));
        applicationUserRepository.save(buyer);

        TradingUser seller = getUserForOrder(bookOrder.getSide() == Side.SELL ? bookOrder : incomingOrder);
        seller.setAccountBalance(seller.getAccountBalance().add(totalAmount));
        applicationUserRepository.save(seller);
    }

    private TradingUser getUserForOrder(Order order) {
       return applicationUserRepository.getUserById(order.getId()).orElseThrow(() -> new UserNotFoundException(order.getId().toString()));
    }
}
