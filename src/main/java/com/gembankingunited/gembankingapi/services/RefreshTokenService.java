package com.gembankingunited.gembankingapi.services;

import com.gembankingunited.gembankingapi.exceptions.TokenRefreshException;
import com.gembankingunited.gembankingapi.models.RefreshToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


@Service
public class RefreshTokenService {
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    private final AccountService accountService;

    public RefreshTokenService(AccountService accountService) {
        this.accountService = accountService;
    }

    public Optional<RefreshToken> findByDocumentId(String documentId) {
        return accountService.findByToken(documentId);
    }

    public RefreshToken createRefreshToken(String userId) {
        RefreshToken refreshToken = new RefreshToken();
        String token = UUID.randomUUID().toString();

        refreshToken.setDocumentId(userId);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(token);

        accountService.saveRefreshToken(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            accountService.deleteToken(token.getDocumentId());
            throw new TokenRefreshException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }
}
