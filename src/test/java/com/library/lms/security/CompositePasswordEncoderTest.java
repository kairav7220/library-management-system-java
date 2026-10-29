package com.library.lms.security;

import com.lambdaworks.crypto.SCrypt;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompositePasswordEncoderTest {

    private final CompositePasswordEncoder encoder = new CompositePasswordEncoder();

    private static String werkzeugStyle(String password, String salt, int n, int r, int p) throws Exception {
        byte[] derived = SCrypt.scrypt(password.getBytes(StandardCharsets.UTF_8),
                salt.getBytes(StandardCharsets.UTF_8), n, r, p, 64);
        return "scrypt:" + n + ":" + r + ":" + p + "$" + salt + "$" + HexFormat.of().formatHex(derived);
    }

    @Test
    void verifiesWerkzeugScryptHash() throws Exception {
        String stored = werkzeugStyle("s3cret-pass", "0123456789abcdef", 32768, 8, 1);
        assertTrue(encoder.matches("s3cret-pass", stored));
        assertFalse(encoder.matches("wrong-pass", stored));
    }

    @Test
    void verifiesWerkzeugScryptWithAltParams() throws Exception {
        String stored = werkzeugStyle("another-pass", "salt-token_16ch", 16384, 8, 2);
        assertTrue(encoder.matches("another-pass", stored));
        assertFalse(encoder.matches("another-pass-wrong", stored));
    }

    @Test
    void encodesAndVerifiesBCrypt() {
        String encoded = encoder.encode("brand-new-pass");
        assertTrue(encoded.startsWith("$2"));
        assertTrue(encoder.matches("brand-new-pass", encoded));
        assertFalse(encoder.matches("other-pass", encoded));
    }

    @Test
    void rejectsUnknownAndMalformedFormats() {
        assertFalse(encoder.matches("x", "plain-text"));
        assertFalse(encoder.matches("x", "scrypt:32768:8:1$only-salt"));
        assertFalse(encoder.matches("x", "scrypt:1:1:1$salt$deadbeef"));
        assertFalse(encoder.matches("", "$2a$whatever"));
        assertFalse(encoder.matches("x", null));
    }
}
