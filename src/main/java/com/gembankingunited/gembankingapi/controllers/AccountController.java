package com.gembankingunited.gembankingapi.controllers;

import com.gembankingunited.gembankingapi.models.Account;
import com.gembankingunited.gembankingapi.models.AccountInfo;
import com.gembankingunited.gembankingapi.services.AccountService;
import com.gembankingunited.gembankingapi.services.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin("http://localhost:3000")
@RequestMapping("/api/v1")
public class AccountController {
    public AccountService accountService;
    public AuthenticationService authenticationService;

    // This controller defines all of our API endpoints for Accounts.
    @Autowired
    public AccountController(AccountService accountService, AuthenticationService authenticationService){
        this.accountService = accountService;
        this.authenticationService = authenticationService;
    }

    // Get Account API endpoint (GET/Read)
    @GetMapping("/account")
    public AccountInfo getAccount() {
        return accountService.getAccountInfo(authenticationService.getCurrentUser());
    }

    // Update Account API endpoint (PUT/Update)
    @PutMapping("/update")
    public String updateAccount(@RequestBody Account account) {
        return accountService.updateAccount(account);
    }

    // Delete Account API endpoint (Delete)
    @DeleteMapping("/delete")
    public String deleteAccount(@RequestParam String documentId) {
        return accountService.deleteAccount(documentId);
    }

    // This is a test endpoint, if you send a GET request to localhost:port the server is running on it will return
    // a 200 status for OK and the text "Test Get Endpoint is Working!"
    @GetMapping("/test")
    public ResponseEntity<String> testGetEndpoint() { return ResponseEntity.ok("Test Get Endpoint is Working!"); }

}
