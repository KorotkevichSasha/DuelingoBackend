package by.gsu.duelingobackend.service;

import by.gsu.duelingobackend.dto.request.PlayPurchaseRequest;
import by.gsu.duelingobackend.exceptions.InvalidOperationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GooglePlayPurchaseVerifierTest {

    @Test
    void acceptsAuthenticMatchingGooglePlayPayload() throws Exception {
        var keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        String payload = """
                {"packageName":"com.duelrush.app","productId":"gold_100",\
                "purchaseToken":"token-1","purchaseState":0}
                """;
        String signature = sign(payload, keys.getPrivate());
        var verifier = new GooglePlayPurchaseVerifier(
                new ObjectMapper(), Base64.getEncoder().encodeToString(keys.getPublic().getEncoded()));

        verifier.verify(new PlayPurchaseRequest("gold_100", "token-1", payload, signature));
    }

    @Test
    void rejectsPayloadForAnotherPackage() throws Exception {
        var keys = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        String payload = """
                {"packageName":"com.someone.else","productId":"gold_100",\
                "purchaseToken":"token-1","purchaseState":0}
                """;
        var verifier = new GooglePlayPurchaseVerifier(
                new ObjectMapper(), Base64.getEncoder().encodeToString(keys.getPublic().getEncoded()));

        assertThatThrownBy(() -> verifier.verify(new PlayPurchaseRequest(
                "gold_100", "token-1", payload, sign(payload, keys.getPrivate()))))
                .isInstanceOf(InvalidOperationException.class);
    }

    private static String sign(String payload, java.security.PrivateKey privateKey) throws Exception {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initSign(privateKey);
        signer.update(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signer.sign());
    }
}
