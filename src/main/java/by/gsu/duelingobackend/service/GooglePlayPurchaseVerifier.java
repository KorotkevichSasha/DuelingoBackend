package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.request.PlayPurchaseRequest;
import by.gsu.duelingobackend.exceptions.InvalidOperationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class GooglePlayPurchaseVerifier {
    private static final String PACKAGE_NAME = "com.duelrush.app";

    private final ObjectMapper objectMapper;
    private final String licenseKey;

    public GooglePlayPurchaseVerifier(
            ObjectMapper objectMapper,
            @Value("${app.google-play.license-key:}") String licenseKey) {
        this.objectMapper = objectMapper;
        this.licenseKey = licenseKey;
    }

    public void verify(PlayPurchaseRequest request) {
        if (licenseKey.isBlank()) {
            throw new InvalidOperationException("Google Play purchases are temporarily unavailable");
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(licenseKey.replaceAll("\\s", ""));
            var publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
            Signature verifier = Signature.getInstance("SHA1withRSA");
            verifier.initVerify(publicKey);
            verifier.update(request.signedData().getBytes(StandardCharsets.UTF_8));
            if (!verifier.verify(Base64.getDecoder().decode(request.signature()))) {
                throw new InvalidOperationException("Google Play could not verify this purchase");
            }

            JsonNode purchase = objectMapper.readTree(request.signedData());
            boolean matchesRequest = PACKAGE_NAME.equals(purchase.path("packageName").asText())
                    && request.productId().equals(purchase.path("productId").asText())
                    && request.purchaseToken().equals(purchase.path("purchaseToken").asText())
                    && purchase.path("purchaseState").asInt(-1) == 0;
            if (!matchesRequest) {
                throw new InvalidOperationException("Google Play returned invalid purchase data");
            }
        } catch (InvalidOperationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidOperationException("Google Play could not verify this purchase");
        }
    }
}
