package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.request.SignInRequest;
import by.gsu.duelingobackend.dto.request.SignUpRequest;
import by.gsu.duelingobackend.dto.response.JwtAuthenticationResponse;
import by.gsu.duelingobackend.dto.response.RegistrationResponse;
import by.gsu.duelingobackend.dto.response.PasswordResetResponse;
import by.gsu.duelingobackend.exceptions.WrongRefreshTokenException;
import by.gsu.duelingobackend.exceptions.EntityAlreadyExistsException;
import by.gsu.duelingobackend.model.User;
import by.gsu.duelingobackend.model.enums.Role;
import by.gsu.duelingobackend.repository.UserRepository;
import by.gsu.duelingobackend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

import static by.gsu.duelingobackend.util.Constants.INVALID_REFRESH_TOKEN_ERR_MSG;
import static by.gsu.duelingobackend.util.Constants.USER_ALREADY_EXISTS_BY_EMAIL_ERR_MSG;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;

    public RegistrationResponse signUp(SignUpRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        var existingUser = userRepository.findByEmail(normalizedEmail);
        if (existingUser.isPresent()) {
            User existing = existingUser.get();
            boolean canResume = !existing.isEmailVerified()
                    && existing.getUsername().equals(request.username())
                    && passwordEncoder.matches(request.password(), existing.getPassword());
            if (canResume) {
                boolean sent = emailVerificationService.issueCode(existing, true);
                return new RegistrationResponse(normalizedEmail, true, sent);
            }
            throw new EntityAlreadyExistsException(
                    String.format(USER_ALREADY_EXISTS_BY_EMAIL_ERR_MSG, normalizedEmail));
        }

        User user = User.builder()
                .username(request.username())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .emailVerified(false)
                .build();

        User saved = userService.create(user);
        boolean sent = emailVerificationService.issueCode(saved, false);
        return new RegistrationResponse(saved.getEmail(), true, sent);
    }

    public JwtAuthenticationResponse signUpAsAdmin(SignUpRequest request) {

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ADMIN)
                .emailVerified(true)
                .build();

        userService.create(user);

        return generateTokens(new UserDetailsImpl(user));
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
        ));

        userService.recordLogin(request.username());
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        return generateTokens(userDetails);
    }

    public JwtAuthenticationResponse verifyEmail(String email, String code) {
        User user = emailVerificationService.verify(email, code);
        return generateTokens(new UserDetailsImpl(user));
    }

    public RegistrationResponse resendVerification(String email) {
        boolean sent = emailVerificationService.resend(email);
        return new RegistrationResponse(email.trim().toLowerCase(Locale.ROOT), true, sent);
    }

    public PasswordResetResponse requestPasswordReset(String email) {
        return passwordResetService.request(email);
    }

    public PasswordResetResponse confirmPasswordReset(String email, String code, String newPassword) {
        return passwordResetService.confirm(email, code, newPassword);
    }

    public JwtAuthenticationResponse refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);

        var userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
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
