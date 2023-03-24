package com.gembankingunited.gembankingapi.services;

import com.gembankingunited.gembankingapi.enums.PrivacyLevel;
import com.gembankingunited.gembankingapi.exceptions.AccountExistsException;
import com.gembankingunited.gembankingapi.models.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Locale;

@Service
@Slf4j
public class AuthenticationService {
    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private final AccountService accountService;

    @Autowired
    public AuthenticationService(AccountService accountService) {
        this.accountService = accountService;
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
                String accountName = firstName + "'s Checking";

                accountService.createAccount(
                        new Account(
                                documentId,
                                username,
                                passwordEncoder.encode(password),
                                firstName,
                                lastName,
                                address,
                                passwordEncoder.encode(ssn)
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

    public void login(HttpServletRequest request, String username, String password) {
        UsernamePasswordAuthenticationToken authReq =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authManager.authenticate(authReq);
        SecurityContext sc = SecurityContextHolder.getContext();
        sc.setAuthentication(auth);
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", sc);
    }

    public void logout(HttpServletRequest request) {
        HttpSession session;
        SecurityContextHolder.clearContext();
        session= request.getSession(false);
        if(session != null) {
            session.invalidate();
        }
    }
}
