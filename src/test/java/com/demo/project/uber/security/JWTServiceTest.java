package com.demo.project.uber.security;

import com.demo.project.uber.entities.User;
import com.demo.project.uber.entities.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    private JWTService jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();

        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecretKey",
                "thisIsATestSecretKeyThatIsAtLeast32Chars!"
        );

        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setRoles(new HashSet<>(Set.of(Role.RIDER)));
    }

    @Test
    void generateAccessToken_shouldReturnNonNullToken() {
        String token = jwtService.generateAccessToken(user);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateAccessToken_shouldContainUserId() {
        String token = jwtService.generateAccessToken(user);
        Long userId = jwtService.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(user.getId());
    }

    @Test
    void generateAccessToken_shouldGenerateDifferentTokensForDifferentUsers() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("another@test.com");
        anotherUser.setRoles(new HashSet<>(Set.of(Role.DRIVER)));

        String token1 = jwtService.generateAccessToken(user);
        String token2 = jwtService.generateAccessToken(anotherUser);

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void generateRefreshToken_shouldReturnNonNullToken() {
        String token = jwtService.generateRefreshToken(user);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void generateRefreshToken_shouldContainUserId() {
        String token = jwtService.generateRefreshToken(user);
        Long userId = jwtService.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(user.getId());
    }

    @Test
    void accessTokenAndRefreshToken_shouldBeDifferent() {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        assertThat(accessToken).isNotEqualTo(refreshToken);
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        String token = jwtService.generateAccessToken(user);

        Long userId = jwtService.getUserIdFromToken(token);

        assertThat(userId).isEqualTo(1L);
    }

    @Test
    void getUserIdFromToken_shouldThrowException_whenTokenIsInvalid() {
        assertThatThrownBy(() -> jwtService.getUserIdFromToken("invalid.token.here"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getUserIdFromToken_shouldThrowException_whenTokenIsTamperedWith() {
        String token = jwtService.generateAccessToken(user);
        String tamperedToken = token + "tampered";

        assertThatThrownBy(() -> jwtService.getUserIdFromToken(tamperedToken))
                .isInstanceOf(Exception.class);
    }
}