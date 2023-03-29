package com.gembankingunited.gembankingapi.controllers;

import com.gembankingunited.gembankingapi.models.Buddy;
import com.gembankingunited.gembankingapi.models.Request;
import com.gembankingunited.gembankingapi.models.Transaction;
import com.gembankingunited.gembankingapi.services.BuddyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin("http://localhost:3000")
@RequestMapping("/api/v1")
public class BuddyController {
    public BuddyService buddyService;

    @Autowired
    public BuddyController(BuddyService buddyService) {
        this.buddyService = buddyService;
    }

    @GetMapping("/buddies")
    public Buddy getBuddyInfo() {
        return buddyService.getBuddies();
    }

    @GetMapping("/buddy-feed")
    public List<Transaction> getBuddyFeed() {
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
    public ResponseEntity<String> addBuddy(@RequestBody Request buddyRequest) {
        return buddyService.requestBuddy(buddyRequest);
    }

    @PostMapping("/approve-buddy")
    public ResponseEntity<String> approveBuddy(@RequestBody Request id) {
        return buddyService.approveBuddyRequest(id);
    }

    @PostMapping("/deny-buddy")
    public ResponseEntity<String> denyBuddy(@RequestBody Request id) {
        return buddyService.denyBuddyRequest(id);
    }
}
