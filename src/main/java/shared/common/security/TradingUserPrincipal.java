package shared.common.security;

import AuctionUser.domain.models.TradingUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class TradingUserPrincipal implements UserDetails {
    private final UUID id;
    private final String username;
    private final String password;
    private final String email;
    private final String role;

    public TradingUserPrincipal(UUID id, String username, String password, String email, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public static TradingUserPrincipal from(TradingUser tradingUser) {
        return new TradingUserPrincipal(
                tradingUser.getId(),
                tradingUser.getUsername(),
                tradingUser.getPassword(),
                tradingUser.getEmail().getEmail(),
                tradingUser.getRole().name()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
