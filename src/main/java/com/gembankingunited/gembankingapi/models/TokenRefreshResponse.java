package com.gembankingunited.gembankingapi.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenRefreshResponse {
    private String access_token;
    private String refresh_token;
    private String tokenType = "Bearer";

    public TokenRefreshResponse(String access_token, String refresh_token) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
    }
}
