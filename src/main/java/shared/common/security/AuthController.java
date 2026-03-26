package shared.common.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import shared.common.security.dto.AuthenticatedUserResponse;
import shared.common.security.dto.JwtAuthenticationResponse;
import shared.common.security.dto.LoginRequest;
import shared.common.security.dto.SessionAuthenticationResponse;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/token")
    public JwtAuthenticationResponse tokenLogin(@Valid @RequestBody LoginRequest loginRequest) {
        TradingUserPrincipal principal = authenticate(loginRequest);
        return new JwtAuthenticationResponse(
                "Bearer",
                jwtService.generateToken(principal),
                jwtService.getExpirationSeconds(),
                buildUserResponse(principal, "JWT")
        );
    }

    @PostMapping("/session")
    public SessionAuthenticationResponse sessionLogin(@Valid @RequestBody LoginRequest loginRequest,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {
        TradingUserPrincipal principal = authenticate(loginRequest);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        Cookie sessionCookie = new Cookie("JSESSIONID", request.getSession(true).getId());
        sessionCookie.setHttpOnly(true);
        sessionCookie.setPath("/");
        response.addCookie(sessionCookie);

        return new SessionAuthenticationResponse(
                request.getSession(true).getId(),
                buildUserResponse(principal, "SESSION")
        );
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse currentUser(Authentication authentication, HttpServletRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof TradingUserPrincipal tradingUserPrincipal) {
            return buildUserResponse(tradingUserPrincipal, resolveAuthenticationType(request));
        }

        if (principal instanceof OAuth2User oauth2User) {
            return new AuthenticatedUserResponse(
                    oauth2User.getAttribute("sub"),
                    oauth2User.getAttribute("name"),
                    oauth2User.getAttribute("email"),
                    "OAUTH2_USER",
                    "OAUTH2"
            );
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unsupported authentication principal");
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    private TradingUserPrincipal authenticate(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );
        return (TradingUserPrincipal) authentication.getPrincipal();
    }

    private AuthenticatedUserResponse buildUserResponse(TradingUserPrincipal principal, String authenticationType) {
        return new AuthenticatedUserResponse(
                principal.getId().toString(),
                principal.getUsername(),
                principal.getEmail(),
                principal.getRole(),
                authenticationType
        );
    }

    private String resolveAuthenticationType(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ")
                ? "JWT"
                : "SESSION";
    }
}
