package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.security.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String TOKEN_VERSION_CLAIM = "token_version";

    @Value("${jwt.access-token.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private Long refreshTokenExpiration;

    @Value("${jwt.token.signing.key}")
    private String jwtSigningKey;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE);
        if (userDetails instanceof UserDetailsImpl userDetailsImpl) {
            claims.put("id", userDetailsImpl.getUser().getId());
            claims.put("email", userDetailsImpl.getUser().getEmail());
            claims.put("role", "ROLE_" + userDetailsImpl.getUser().getRole().name());
            claims.put(TOKEN_VERSION_CLAIM, userDetailsImpl.getUser().getTokenVersion());
        }
        return generateToken(claims, userDetails, accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE);
        if (userDetails instanceof UserDetailsImpl userDetailsImpl) {
            claims.put(TOKEN_VERSION_CLAIM, userDetailsImpl.getUser().getTokenVersion());
        }
        return generateToken(claims, userDetails, refreshTokenExpiration);
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, ACCESS_TOKEN_TYPE);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, REFRESH_TOKEN_TYPE);
    }

    private boolean isTokenValid(String token, UserDetails userDetails, String expectedType) {
        Claims claims = extractAllClaims(token);
        String username = claims.getSubject();
        String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
        Object rawVersion = claims.get(TOKEN_VERSION_CLAIM);
        int tokenVersion = rawVersion instanceof Number number ? number.intValue() : 0;
        int currentVersion = userDetails instanceof UserDetailsImpl userDetailsImpl
                ? userDetailsImpl.getUser().getTokenVersion()
                : 0;
        return username != null
                && username.equals(userDetails.getUsername())
                && expectedType.equals(tokenType)
                && tokenVersion == currentVersion
                && claims.getExpiration().after(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationTime) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] encodedKeyBytes = Decoders.BASE64.decode(jwtSigningKey);
        return Keys.hmacShaKeyFor(encodedKeyBytes);
    }
}
