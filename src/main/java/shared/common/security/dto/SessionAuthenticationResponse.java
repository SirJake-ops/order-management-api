package shared.common.security.dto;

public record SessionAuthenticationResponse(
        String sessionId,
        AuthenticatedUserResponse user
) {
}
