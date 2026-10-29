package com.library.lms.security;

import com.lambdaworks.crypto.SCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class CompositePasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        if (encodedPassword.startsWith("scrypt:")) {
            return matchesWerkzeugScrypt(rawPassword.toString(), encodedPassword);
        }
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }
        return false;
    }

    private boolean matchesWerkzeugScrypt(String rawPassword, String stored) {
        try {
            String[] parts = stored.split("\\$");
            if (parts.length != 3) {
                return false;
            }
            String[] params = parts[0].substring("scrypt:".length()).split(":");
            if (params.length != 3) {
                return false;
            }
            int n = Integer.parseInt(params[0]);
            int r = Integer.parseInt(params[1]);
            int p = Integer.parseInt(params[2]);
            byte[] salt = parts[1].getBytes(StandardCharsets.UTF_8);
            byte[] expected = HexFormat.of().parseHex(parts[2]);
            byte[] actual = SCrypt.scrypt(rawPassword.getBytes(StandardCharsets.UTF_8), salt, n, r, p, expected.length);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }
}
