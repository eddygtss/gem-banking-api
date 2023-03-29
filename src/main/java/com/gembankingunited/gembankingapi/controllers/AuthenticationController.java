package com.gembankingunited.gembankingapi.controllers;

import com.gembankingunited.gembankingapi.configs.JwtUtils;
import com.gembankingunited.gembankingapi.exceptions.TokenRefreshException;
import com.gembankingunited.gembankingapi.models.*;
import com.gembankingunited.gembankingapi.services.AccountService;
import com.gembankingunited.gembankingapi.services.AuthenticationService;
import com.gembankingunited.gembankingapi.services.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin("http://localhost:3000")
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final AccountService accountService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;


    public AuthenticationController(AuthenticationService authenticationService,
                                    AccountService accountService,
                                    RefreshTokenService refreshTokenService,
                                    JwtUtils jwtUtils) {
        this.authenticationService = authenticationService;
        this.accountService = accountService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtils = jwtUtils;
    }

    // Create new user account API endpoint (POST/Create)
    @PostMapping("/register")
    public ResponseEntity<Void> createAccount(@RequestBody Account createAccountRequest) {
        authenticationService.createUser(createAccountRequest);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            JwtResponse jwt = authenticationService.login(response, request);
            if (jwt.getAccess_token() != null) {
                return ResponseEntity.ok(jwt);
            }
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(final HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(request, response);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        Optional<RefreshToken> refreshTokenOptional = accountService.findByToken(requestRefreshToken);
        RefreshToken refreshToken = null;

        refreshTokenOptional.ifPresent(refreshTokenService::verifyExpiration);

        if (refreshTokenOptional.isPresent()) {
            refreshToken = refreshTokenOptional.get();
        }

        if (refreshToken != null) {
            String token = jwtUtils.createToken(refreshToken.getDocumentId());
            return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
        } else {
            throw new TokenRefreshException(requestRefreshToken,
                    "Refresh token is not in database!");
        }
    }
}
