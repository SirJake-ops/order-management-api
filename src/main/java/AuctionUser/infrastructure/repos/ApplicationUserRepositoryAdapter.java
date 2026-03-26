package AuctionUser.infrastructure.repos;

import AuctionUser.domain.IApplicationUserRepository;
import AuctionUser.domain.models.TradingUser;
import AuctionUser.infrastructure.persistence.ApplicationUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ApplicationUserRepositoryAdapter implements IApplicationUserRepository {

    private final ApplicationUserRepository applicationUserRepository;

    public ApplicationUserRepositoryAdapter(ApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }

    @Override
    public Optional<TradingUser> getUserById(UUID id) {
        return applicationUserRepository.findById(id);
    }

    @Override
    public Optional<TradingUser> findByUsername(String username) {
        return applicationUserRepository.findByUsername(username);
    }

    @Override
    public Optional<TradingUser> findByEmail(String email) {
        return applicationUserRepository.findByEmail_Email(email);
    }

    @Override
    public TradingUser save(TradingUser tradingUser) {
        return applicationUserRepository.save(tradingUser);
    }
}
