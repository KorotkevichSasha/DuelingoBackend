package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.request.SignInRequest;
import by.gsu.duelingobackend.dto.request.SignUpRequest;
import by.gsu.duelingobackend.dto.response.JwtAuthenticationResponse;
import by.gsu.duelingobackend.exceptions.WrongRefreshTokenException;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.enums.Role;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static by.gsu.duelingobackend.util.Constants.INVALID_REFRESH_TOKEN_ERR_MSG;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponse signUp(SignUpRequest request) {

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userService.create(user);

        return generateTokens(new UserDetailsImpl(user));
    }

    public JwtAuthenticationResponse signUpAsAdmin(SignUpRequest request) {

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ADMIN)
                .build();

        userService.create(user);

        return generateTokens(new UserDetailsImpl(user));
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
        ));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        return generateTokens(userDetails);
    }

    public JwtAuthenticationResponse refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);

        var userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtService.isTokenValid(refreshToken, userDetails)) {
            return refreshAccessToken(userDetails, refreshToken);
        }

        throw new WrongRefreshTokenException(INVALID_REFRESH_TOKEN_ERR_MSG);
    }

    private JwtAuthenticationResponse generateTokens(UserDetails user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new JwtAuthenticationResponse(accessToken, refreshToken);
    }

    private JwtAuthenticationResponse refreshAccessToken(UserDetails userDetails, String refreshToken) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        return new JwtAuthenticationResponse(accessToken, refreshToken);
    }
}
