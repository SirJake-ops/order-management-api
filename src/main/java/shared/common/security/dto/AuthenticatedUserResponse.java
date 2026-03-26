package shared.common.security.dto;

public record AuthenticatedUserResponse(
        String id,
        String username,
        String email,
        String role,
        String authenticationType
) {
}
