package com.gembankingunited.gembankingapi.models;

import com.gembankingunited.gembankingapi.enums.PrivacyLevel;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@Builder
@RequiredArgsConstructor
public class Buddy {
    private String documentId;
    private PrivacyLevel privacySetting;
    private List<Profile> buddyList;
    private List<Request> buddyRequests;
    private List<Transaction> buddyTransactions;

    public Buddy(String documentId, PrivacyLevel privacySetting, List<Profile> buddyList, List<Request> buddyRequests, List<Transaction> buddyTransactions) {
        this.documentId = documentId;
        this.privacySetting = privacySetting;
        this.buddyList = buddyList;
        this.buddyRequests = buddyRequests;
        this.buddyTransactions = buddyTransactions;
    }

    public List<Transaction> getBuddyTransactions() {
        return buddyTransactions;
    }
    public List<Profile> getBuddyList() {
        return buddyList;
    }
    public List<Request> getBuddyRequests() {
        return buddyRequests;
    }
}
