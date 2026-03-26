package shared.common.security.dto;

public record JwtAuthenticationResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        AuthenticatedUserResponse user
) {
}
