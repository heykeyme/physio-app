package com.alpro.physio.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TokenBlacklist {

    private final Set<String> blacklist = new HashSet<>();
    private final JwtUtil jwtUtil;

    public TokenBlacklist(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public void blacklistToken(String token) {
        // Optionally only add if token is still valid
        if (jwtUtil.isTokenValid(token)) {
            blacklist.add(token);
        }
    }

    public boolean isBlacklisted(String token) {
        // Remove expired tokens before checking
        cleanExpiredTokens();
        return blacklist.contains(token);
    }

    @Scheduled(fixedDelay = 60000) // run every minute to clean up
    private void cleanExpiredTokens() {
        blacklist.removeIf(token -> !jwtUtil.isTokenValid(token));
    }
}