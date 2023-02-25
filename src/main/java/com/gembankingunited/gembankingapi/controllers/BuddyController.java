package com.gembankingunited.gembankingapi.controllers;

import com.gembankingunited.gembankingapi.exceptions.AccountInvalidException;
import com.gembankingunited.gembankingapi.models.*;
import com.gembankingunited.gembankingapi.services.BuddyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class BuddyController {
    @Autowired
    public BuddyService buddyService;

    @GetMapping("/buddies")
    public Buddy getBuddyInfo() throws InterruptedException, ExecutionException, AccountInvalidException {
        return buddyService.getBuddies();
    }

    @GetMapping("/buddy-feed")
    public List<Transaction> getBuddyFeed() throws ExecutionException, AccountInvalidException, InterruptedException {
        return buddyService.getBuddiesTransactions(getBuddyInfo());
    }

//    @PutMapping("/update-buddies")
//    public ResponseEntity<String> updateAccountsBuddies() throws Exception {
//        List<String> allAccounts = accountService.getAllDocumentIds();
//        int num = 0;
//
//        for (String documentId: allAccounts) {
//            accountService.updateBuddy(
//                    new Buddy(
//                    documentId,
//                    PrivacyLevel.PRIVATE,
//                    new ArrayList<>(),
//                    new ArrayList<>(),
//                    new ArrayList<>()
//            ));
//            num += 1;
//        }
//
//        return ResponseEntity.ok("Updated " + num + " accounts.");
//    }

    @PostMapping("/add-buddy")
    public ResponseEntity<String> addBuddy(@RequestBody Request buddyRequest) throws Exception {
        return buddyService.requestBuddy(buddyRequest);
    }

    @PostMapping("/approve-buddy")
    public ResponseEntity<String> approveBuddy(@RequestBody String id) throws Exception {
        return buddyService.approveBuddyRequest(id);
    }

    @PostMapping("/deny-buddy")
    public ResponseEntity<String> denyBuddy(@RequestBody String id) throws Exception {
        return buddyService.denyBuddyRequest(id);
    }
}
