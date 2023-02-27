package com.gembankingunited.gembankingapi.services;

import com.gembankingunited.gembankingapi.enums.PrivacyLevel;
import com.gembankingunited.gembankingapi.enums.Status;
import com.gembankingunited.gembankingapi.exceptions.AccountInvalidException;
import com.gembankingunited.gembankingapi.models.Buddy;
import com.gembankingunited.gembankingapi.models.Profile;
import com.gembankingunited.gembankingapi.models.Transaction;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuddyServiceTest {
    @Mock
    BuddyService buddyService;

    @Mock
    AuthenticationService authenticationService;

    @Mock
    AccountService accountService;

    @BeforeEach
    void setUp() {
        buddyService = new BuddyService(accountService, authenticationService);
    }

    @Test
    void getBuddies_returnsBuddies_whenValidUser() {
        Buddy expected = getStubBuddies();

        when(authenticationService.getCurrentUser()).thenReturn("user_test1@gembanking.com");
        when(accountService.getBuddy(anyString())).thenReturn(getStubBuddies());
        Buddy actual = buddyService.getBuddies();

        assertEquals(expected, actual);
    }

//    @Test
//    void getBuddies_returnsEmptyBuddies_whenInValidUser() {
//        Buddy expected = Buddy.builder().build();
//
//        when(authenticationService.getCurrentUser()).thenReturn("user_test1@gembanking.com");
//        when(accountService.getBuddy(anyString())).thenThrow(new AccountInvalidException());
//        Buddy actual = buddyService.getBuddies();
//
//        assertEquals(expected, actual);
//    }

    @Test
    void getBuddiesTransactions_returnsTransactions_whenValidBuddy() {
        List<Transaction> expected = List.of(Transaction.builder()
                .amount(10.00)
                .date("12/12/2022")
                .id("123")
                .memo("Test")
                .transactionStatus(Status.APPROVED)
                .build());

        when(accountService.getBuddyTransactions(any())).thenReturn(List.of(Transaction.builder()
                .amount(10.00)
                .date("12/12/2022")
                .id("123")
                .memo("Test")
                .transactionStatus(Status.APPROVED)
                .build()));

        List<Transaction> actual = buddyService.getBuddiesTransactions(getStubBuddies());

        assertEquals(expected, actual);
    }

//    @Test
//    void getBuddiesTransactions_returnsEmptyTransactions_whenInValidBuddy() {
//        List<Transaction> expected = new ArrayList<>();
//
//        when(accountService.getBuddyTransactions(any())).thenThrow(new Exception());
//
//        List<Transaction> actual = buddyService.getBuddiesTransactions(getStubBuddies());
//
//        assertEquals(expected, actual);
//    }

    @Test
    void requestBuddy() {
    }

    @Test
    void approveBuddyRequest() {
    }

    @Test
    void denyBuddyRequest() {
    }


    private Profile getStubProfile1() {
        return Profile.builder()
                .documentId("user_test1@gembanking.com")
                .privacySetting(PrivacyLevel.PRIVATE)
                .firstName("Test")
                .lastName("One")
                .username("test1@gembanking.com")
                .status("Test!")
                .build();
    }

    private Profile getStubProfile2() {
        return Profile.builder()
                .documentId("user_test2@gembanking.com")
                .privacySetting(PrivacyLevel.PRIVATE)
                .firstName("Test")
                .lastName("Two")
                .username("test2@gembanking.com")
                .status("Test 2!")
                .build();
    }
    private Buddy getStubBuddies() {
        return Buddy.builder()
                .documentId("user_test2@gembanking.com")
                .buddyList(List.of(getStubProfile1()))
                .build();
    }
}