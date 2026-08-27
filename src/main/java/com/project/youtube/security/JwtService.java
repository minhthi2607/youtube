package com.project.youtube.security;

import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal self-contained JWT (HS256) implementation.
 * Avoids pulling in a dedicated JWT library: signing/verification is plain
 * HMAC-SHA256 (javax.crypto, JDK-provided) and claim encoding reuses Jackson,
 * which is already on the classpath via spring-boot-starter-webmvc.
 */
@Component
public class JwtService {

    private static final String ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecretKeySpec signingKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.expiration-ms}") long expirationMs) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 bytes long");
        }
        this.signingKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long userId, String username) {
        try {
            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            long nowSeconds = Instant.now().getEpochSecond();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", username);
            payload.put("uid", userId);
            payload.put("iat", nowSeconds);
            payload.put("exp", nowSeconds + (expirationMs / 1000));

            String headerSegment = ENCODER.encodeToString(objectMapper.writeValueAsBytes(header));
            String payloadSegment = ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
            String signingInput = headerSegment + "." + payloadSegment;
            String signatureSegment = ENCODER.encodeToString(sign(signingInput));

            return signingInput + "." + signatureSegment;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate JWT", e);
        }
    }

    public ParsedToken parseAndValidate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new JwtException("Malformed JWT");
        }
        String signingInput = parts[0] + "." + parts[1];
        byte[] expectedSignature = sign(signingInput);
        byte[] actualSignature = DECODER.decode(parts[2]);
        if (!MessageDigest.isEqual(expectedSignature, actualSignature)) {
            throw new JwtException("Invalid JWT signature");
        }

        Map<?, ?> payload;
        try {
            payload = objectMapper.readValue(DECODER.decode(parts[1]), Map.class);
        } catch (Exception e) {
            throw new JwtException("Invalid JWT payload");
        }

        long exp = ((Number) payload.get("exp")).longValue();
        if (Instant.now().getEpochSecond() > exp) {
            throw new JwtException("JWT has expired");
        }

        String username = (String) payload.get("sub");
        Long userId = ((Number) payload.get("uid")).longValue();
        return new ParsedToken(userId, username);
    }

    private byte[] sign(String input) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(signingKey);
            return mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | java.security.InvalidKeyException e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }

    public record ParsedToken(Long userId, String username) {
    }

    public static class JwtException extends RuntimeException {
        public JwtException(String message) {
            super(message);
        }
    }
}
