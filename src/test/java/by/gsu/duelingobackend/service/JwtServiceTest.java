package by.gsu.duelingobackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

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
}
