package com.gembankingunited.gembankingapi.controllers;

import com.gembankingunited.gembankingapi.exceptions.AccountExistsException;
import com.gembankingunited.gembankingapi.exceptions.AccountInvalidException;
import com.gembankingunited.gembankingapi.models.Account;
import com.gembankingunited.gembankingapi.models.AccountInfo;
import com.gembankingunited.gembankingapi.services.AccountService;
import com.gembankingunited.gembankingapi.services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class AccountController {
    @Autowired
    public AccountService accountService;
    @Autowired
    public AuthenticationService authenticationService;

    // This controller defines all of our API endpoints for Accounts.
    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    // Create new user account API endpoint (POST/Create)
    @PostMapping("/register")
    public ResponseEntity<Void> createAccount(@RequestBody Account createAccountRequest) throws InterruptedException, ExecutionException, AccountExistsException {
        authenticationService.createUser(createAccountRequest);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // Get Account API endpoint (GET/Read)
    @GetMapping("/account")
    public AccountInfo getAccount() throws InterruptedException, ExecutionException, AccountInvalidException {
        return accountService.getAccountInfo(authenticationService.getCurrentUser());
    }

    // Update Account API endpoint (PUT/Update)
    @PutMapping("/update")
    public String updateAccount(@RequestBody Account account) throws InterruptedException, ExecutionException {
        return accountService.updateAccount(account);
    }

    // Delete Account API endpoint (Delete)
    @DeleteMapping("/delete")
    public String deleteAccount(@RequestParam String documentId) throws InterruptedException, ExecutionException {
        return accountService.deleteAccount(documentId);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<Void> login(@RequestBody Account loginAccountRequest, final HttpServletRequest request) {
        authenticationService.login(request, loginAccountRequest.getUsername().toLowerCase(), loginAccountRequest.getPassword());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout() {
        authenticationService.logout();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // This is a test endpoint, if you send a GET request to localhost:port the server is running on it will return
    // a 200 status for OK and the text "Test Get Endpoint is Working!"
    @GetMapping("/test")
    public ResponseEntity<String> testGetEndpoint() { return ResponseEntity.ok("Test Get Endpoint is Working!"); }

}
