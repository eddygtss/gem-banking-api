package com.gembankingunited.gembankingapi.controllers;

import com.gembankingunited.gembankingapi.enums.PrivacyLevel;
import com.gembankingunited.gembankingapi.exceptions.AccountInvalidException;
import com.gembankingunited.gembankingapi.models.Account;
import com.gembankingunited.gembankingapi.models.Profile;
import com.gembankingunited.gembankingapi.services.AccountService;
import com.gembankingunited.gembankingapi.services.AuthenticationService;
import com.gembankingunited.gembankingapi.services.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class ProfileController {
    @Autowired
    public AccountService accountService;
//    @Autowired
//    public ProfileService profileService;
    @Autowired
    public AuthenticationService authenticationService;

    @GetMapping("/profile")
    public Profile getProfile() throws InterruptedException, ExecutionException, AccountInvalidException {
        return accountService.getProfile(authenticationService.getCurrentUser());
    }

    @PutMapping("/update-status")
    public ResponseEntity<String> updateStatus(@RequestBody String status) throws Exception {
        if (status.length() >= 100) {
            return ResponseEntity.badRequest().body("Status text limit is 100 characters.");
        }

        Profile profile = accountService.getProfile(authenticationService.getCurrentUser());
        profile.setStatus(status);

        accountService.updateProfile(profile);

        return ResponseEntity.ok("Updated status successfully.");
    }

    @PutMapping("/update-username")
    public ResponseEntity<String> updateUsername(@RequestBody String username) throws Exception {
        if (username.length() >= 16) {
            return ResponseEntity.badRequest().body("Username must be less than 16 characters.");
        }
        List<String> usernames = accountService.getAllUsernames();

        for (String user: usernames) {
            if (user.equalsIgnoreCase(username)){
                return ResponseEntity.badRequest().body("This username is already taken.");
            }
        }

        Profile profile = accountService.getProfile(authenticationService.getCurrentUser());
        Account account = accountService.getAccount(authenticationService.getCurrentUser());
        account.setUsername(username);
        profile.setUsername(username);

        accountService.updateProfile(profile);

        return ResponseEntity.ok("Updated status successfully.");
    }

//    @PutMapping("/update-profiles")
//    public ResponseEntity<String> updateAccountsProfiles() throws Exception {
//        List<Account> allAccounts = accountService.getAllAccounts();
//        int num = 0;
//
//        for (Account account: allAccounts) {
//            accountService.updateProfile(
//                    new Profile(
//                            account.getDocumentId(),
//                            account.getUsername(),
//                            account.getFirstName(),
//                            account.getLastName(),
//                            PrivacyLevel.PRIVATE,
//                            "Hi, I'm new to Gem Banking!"
//                    ));
//            num += 1;
//        }
//
//        return ResponseEntity.ok("Updated " + num + " accounts.");
//    }


}
