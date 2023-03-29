package com.gembankingunited.gembankingapi.models;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
public class JwtResponse {
    private String access_token;
    private String type = "Bearer";
    private String refresh_token;
    private Long id;
    private String username;
    private String email;
    private List<String> roles;

    public JwtResponse(String access_token,
                       String type,
                       String refresh_token,
                       Long id,
                       String username,
                       String email,
                       List<String> roles) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
