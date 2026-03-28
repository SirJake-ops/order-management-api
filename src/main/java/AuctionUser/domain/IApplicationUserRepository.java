package AuctionUser.domain;

import AuctionUser.domain.models.TradingUser;

import java.util.Optional;
import java.util.UUID;

public interface IApplicationUserRepository {
    Optional<TradingUser> getUserById(UUID id);
    Optional<TradingUser> findByUsername(String username);
    Optional<TradingUser> findByEmail(String email);
    TradingUser save(TradingUser tradingUser);
}
