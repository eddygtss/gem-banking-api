package com.gembankingunited.gembankingapi.models;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
public class RefreshToken {
    @Id
    private String documentId;
    private String token;
    private Instant expiryDate;

    public RefreshToken(String documentId, String token, Instant expiryDate) {
        this.documentId = documentId;
        this.token = token;
        this.expiryDate = expiryDate;
    }
}