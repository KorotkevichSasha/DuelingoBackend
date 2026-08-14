package by.gsu.duelingobackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import by.gsu.duelingobackend.model.enums.Role;
import by.gsu.duelingobackend.security.UserDetailsImpl;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();
    private final UserDetails user = User.withUsername("valid-user")
            .password("irrelevant")
            .authorities("ROLE_USER")
            .build();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtSigningKey",
                "VGhpc0lzQVRlc3RTaWduaW5nS2V5VGhhdElzTG9uZ0Vub3VnaCE=");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 60_000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 120_000L);
    }

    @Test
    void accessAndRefreshTokensCannotBeInterchanged() {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        assertThat(jwtService.isAccessTokenValid(accessToken, user)).isTrue();
        assertThat(jwtService.isRefreshTokenValid(refreshToken, user)).isTrue();
        assertThat(jwtService.isAccessTokenValid(refreshToken, user)).isFalse();
        assertThat(jwtService.isRefreshTokenValid(accessToken, user)).isFalse();
    }

    @Test
    void passwordResetVersionInvalidatesPreviouslyIssuedTokens() {
        var account = by.gsu.duelingobackend.model.User.builder()
                .username("versioned-user")
                .password("irrelevant")
                .email("versioned@example.com")
                .emailVerified(true)
                .role(Role.USER)
                .tokenVersion(0)
                .build();
        var details = new UserDetailsImpl(account);
        String accessToken = jwtService.generateAccessToken(details);
        String refreshToken = jwtService.generateRefreshToken(details);

        account.setTokenVersion(1);

        assertThat(jwtService.isAccessTokenValid(accessToken, details)).isFalse();
        assertThat(jwtService.isRefreshTokenValid(refreshToken, details)).isFalse();
    }
}
