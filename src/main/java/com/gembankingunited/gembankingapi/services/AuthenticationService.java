package com.gembankingunited.gembankingapi.services;

import com.gembankingunited.gembankingapi.configs.JwtUtils;
import com.gembankingunited.gembankingapi.enums.PrivacyLevel;
import com.gembankingunited.gembankingapi.exceptions.AccountExistsException;
import com.gembankingunited.gembankingapi.models.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Locale;

@Slf4j
@Component
public class AuthenticationService {
    BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final AccountService accountService;
    private final UserService userDetailsService;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthenticationService(AccountService accountService,
                                 AuthenticationManager authManager,
                                 BCryptPasswordEncoder passwordEncoder,
                                 UserService userDetailsService,
                                 JwtUtils jwtUtils,
                                 RefreshTokenService refreshTokenService) {
        this.accountService = accountService;
        this.authManager = authManager;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    public String getCurrentUser() {
        try {
            String username;
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }

            return "user_" + username;
        } catch (Exception e) {
            log.error(e.getMessage());
            return e.getMessage();
        }
    }

    public void createUser(Account account) {
        String documentId = "user_" + account.getUsername().toLowerCase(Locale.ROOT);

        try {
            if (accountService.getAccount(documentId) == null) {
                String username = account.getUsername().toLowerCase(Locale.ROOT);
                String password = account.getPassword();
                String firstName = account.getFirstName();
                String lastName = account.getLastName();
                String ssn = account.getSsn();
                String address = account.getAddress();
                String roles = account.getRoles();
                String accountName = firstName + "'s Checking";

                accountService.createAccount(
                        new Account(
                                documentId,
                                username,
                                passwordEncoder.encode(password),
                                firstName,
                                lastName,
                                address,
                                passwordEncoder.encode(ssn),
                                roles
                        ),
                        new AccountInfo(
                                documentId,
                                accountName,
                                100.00,
                                new ArrayList<>(),
                                new ArrayList<>()),
                        new Buddy(
                                documentId,
                                PrivacyLevel.PRIVATE,
                                new ArrayList<>(),
                                new ArrayList<>(),
                                new ArrayList<>()
                        ),
                        new Profile(
                                documentId,
                                username,
                                firstName,
                                lastName,
                                PrivacyLevel.PRIVATE,
                                "Hi, I'm new to Gem Banking!"
                        )
                );
            } else {
                throw new AccountExistsException("An account with this email already exists!");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public JwtResponse login(HttpServletResponse response, LoginRequest loginRequest) {
        JwtResponse jwt = JwtResponse.builder().build();
        UsernamePasswordAuthenticationToken authReq =
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername().toLowerCase(), loginRequest.getPassword());

        authManager.authenticate(authReq);

        final UserDetails user = userDetailsService.loadUserByUsername(loginRequest.getUsername().toLowerCase());

        if (user != null) {
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
            String jwtLiteral = jwtUtils.generateToken(user);
            Cookie cookie = new Cookie("access_token", jwtLiteral);
            cookie.setMaxAge(3 * 60); // expires in 3 min
//            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setPath("/"); // Global
            response.addCookie(cookie);

            jwt.setAccess_token(jwtLiteral);
            jwt.setRefresh_token(refreshToken.getToken());
            jwt.setUsername(user.getUsername());
            jwt.setEmail(user.getUsername());
        }

        return jwt;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }

        HttpSession session;
        SecurityContextHolder.clearContext();
        session= request.getSession(false);
        if(session != null) {
            session.invalidate();
        }
    }
}
