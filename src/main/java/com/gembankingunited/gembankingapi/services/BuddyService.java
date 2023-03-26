package com.gembankingunited.gembankingapi.services;

import com.gembankingunited.gembankingapi.enums.Status;
import com.gembankingunited.gembankingapi.exceptions.BuddyExistsException;
import com.gembankingunited.gembankingapi.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class BuddyService {
    public AccountService accountService;
    public AuthenticationService authenticationService;

    public BuddyService(AccountService accountService, AuthenticationService authenticationService) {
        this.accountService = accountService;
        this.authenticationService = authenticationService;
    }

    public Buddy getBuddies() {
        Buddy userBuddies = Buddy.builder().build();
        try {
            String currentUser = authenticationService.getCurrentUser();

            userBuddies = accountService.getBuddy(currentUser);

            return userBuddies;
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }

        return userBuddies;
    }

    public List<Transaction> getBuddiesTransactions(Buddy buddyInfo) {
        List<Transaction> transactions = new ArrayList<>();
        try {
            transactions = accountService.getBuddyTransactions(buddyInfo);
            return transactions;
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }

        return transactions;
    }

    public ResponseEntity<String> requestBuddy(Request buddyRequest) {
        String requester = authenticationService.getCurrentUser();
        String responder = buddyRequest.getResponder().toLowerCase();
        buddyRequest.setResponder(responder);

        if (requester.substring(5).equals(buddyRequest.getResponder())){
            return ResponseEntity.badRequest().body("You cannot add yourself as a buddy.");
        }

        Buddy requesterBuddy = accountService.getBuddy(requester);
        buddyRequest.setRequester(requester.substring(5));

        try {
            Buddy responderBuddy = accountService.getBuddy("user_" + buddyRequest.getResponder());

            List<Buddy> updatedBuddies = new ArrayList<>();
            List<Profile> buddyList = requesterBuddy.getBuddyList();
            List<Request> requestersRequestList = requesterBuddy.getBuddyRequests();
            List<Request> respondersRequestList = responderBuddy.getBuddyRequests();

            for (Profile buddy:buddyList) {
                if (buddy.getDocumentId().equals(responder)) {
                    throw new BuddyExistsException("You are already buddies with " + responder);
                }
            }

            for (Request request:requestersRequestList) {
                if (buddyRequest.getId().equals(request.getId())) {
                    throw new BuddyExistsException("You have already added this buddy and the request is pending, please wait until the request is approved or denied.");
                }
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String currentTime = LocalDateTime.now().format(formatter);
            buddyRequest.setDate(currentTime);

            buddyRequest.setRequestStatus(Status.PENDING);

            respondersRequestList.add(buddyRequest);
            responderBuddy.setBuddyRequests(respondersRequestList);

            Request requesterRequest = new Request(buddyRequest);
            requesterRequest.setRequestStatus(Status.SENT);

            requestersRequestList.add(requesterRequest);
            requesterBuddy.setBuddyRequests(requestersRequestList);

            updatedBuddies.add(requesterBuddy);
            updatedBuddies.add(responderBuddy);

            for (Buddy buddy: updatedBuddies) {
                accountService.updateBuddy(buddy);
            }
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

        return new ResponseEntity<>("Successfully sent " + buddyRequest.getResponder() + " a buddy request.", HttpStatus.CREATED);
    }

    public ResponseEntity<String> approveBuddyRequest(Request parsed) throws Exception {
        String currentUser = authenticationService.getCurrentUser();
        Buddy responderBuddy = accountService.getBuddy(currentUser);
        Profile responderProfile = accountService.getProfile(currentUser);
        List<Request> responderRequests = responderBuddy.getBuddyRequests();

        for (Request request: responderRequests) {
            if (request.getId().equals(parsed.getId())) {
                String requester = "user_" + request.getRequester();
                Profile requesterProfile = accountService.getProfile(requester);
                Buddy requesterBuddy = accountService.getBuddy(requester);

                List<Buddy> updatedBuddies = new ArrayList<>();
                List<Profile> requesterBuddyList = requesterBuddy.getBuddyList();
                List<Profile> responderBuddyList = responderBuddy.getBuddyList();

                List<Request> requestersRequestList = requesterBuddy.getBuddyRequests();
                List<Request> respondersRequestList = responderBuddy.getBuddyRequests();

                respondersRequestList.remove(request);
                requestersRequestList.removeIf(requesterRequest -> requesterRequest.getId().equals(request.getId()));

                requesterBuddyList.add(responderProfile);
                requesterBuddy.setBuddyList(requesterBuddyList);

                responderBuddyList.add(requesterProfile);
                responderBuddy.setBuddyList(responderBuddyList);

                updatedBuddies.add(requesterBuddy);
                updatedBuddies.add(responderBuddy);

                for (Buddy buddy: updatedBuddies) {
                    accountService.updateBuddy(buddy);
                }

                return ResponseEntity.ok("You are now buddies with " + requesterProfile.getFirstName() + " " + requesterProfile.getLastName());
            }
        }

        return ResponseEntity.badRequest().body("There was an error approving this request.");
    }

    public ResponseEntity<String> denyBuddyRequest(Request parsed) throws Exception {
        Buddy responderBuddy = accountService.getBuddy(authenticationService.getCurrentUser());
        List<Request> responderBuddyRequests = responderBuddy.getBuddyRequests();

        for (Request request: responderBuddyRequests) {
            if (request.getId().equals(parsed.getId())) {
                String requester = "user_" + request.getRequester();
                Account requesterAccount = accountService.getAccount(requester);
                Buddy requesterBuddy = accountService.getBuddy(requester);

                List<Buddy> updatedBuddies = new ArrayList<>();
                List<Request> requestersRequestList = requesterBuddy.getBuddyRequests();
                List<Request> respondersRequestList = responderBuddy.getBuddyRequests();

                respondersRequestList.remove(request);
                requestersRequestList.removeIf(requesterRequest -> requesterRequest.getId().equals(request.getId()));

                updatedBuddies.add(requesterBuddy);
                updatedBuddies.add(responderBuddy);

                for (Buddy buddy: updatedBuddies) {
                    accountService.updateBuddy(buddy);
                }

                return ResponseEntity.ok("Denied buddy request from " + requesterAccount.getFirstName() + " " + requesterAccount.getLastName());
            }
        }

        return ResponseEntity.badRequest().body("There was an error denying this request.");
    }
}
