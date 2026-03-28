package shared.common.security;

import AuctionUser.domain.models.TradingUser;
import AuctionUser.domain.IApplicationUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TradingUserDetailsService implements UserDetailsService, UserDetailsPasswordService {
    private final IApplicationUserRepository applicationUserRepository;

    public TradingUserDetailsService(IApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TradingUser tradingUser = applicationUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return TradingUserPrincipal.from(tradingUser);
    }

    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        TradingUser tradingUser = applicationUserRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + user.getUsername()));
        tradingUser.setPassword(newPassword);
        applicationUserRepository.save(tradingUser);
        return TradingUserPrincipal.from(tradingUser);
    }
}
