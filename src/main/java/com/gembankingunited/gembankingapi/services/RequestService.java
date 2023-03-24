package com.gembankingunited.gembankingapi.services;

import com.gembankingunited.gembankingapi.enums.Status;
import com.gembankingunited.gembankingapi.enums.TransactionType;
import com.gembankingunited.gembankingapi.exceptions.AccountInvalidException;
import com.gembankingunited.gembankingapi.exceptions.InsufficientFundsException;
import com.gembankingunited.gembankingapi.exceptions.InvalidRequesteeException;
import com.gembankingunited.gembankingapi.exceptions.InvalidTransactionException;
import com.gembankingunited.gembankingapi.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class RequestService {
    @Autowired
    public AuthenticationService authenticationService;
    @Autowired
    public AccountService accountService;

    public ResponseEntity<String> Request(Request requestFundsTransaction) {
        String requester = authenticationService.getCurrentUser();
        String responder = requestFundsTransaction.getResponder().toLowerCase();
        requestFundsTransaction.setResponder(responder);

        if (requester.substring(5).equals(responder)){
            return ResponseEntity.badRequest().body("You cannot request money from yourself.");
        }

        AccountInfo requesterAccountInfo = accountService.getAccountInfo(requester);
        requestFundsTransaction.setRequester(requester.substring(5));

        try {
            AccountInfo responderAccountInfo = accountService.getAccountInfo("user_" + responder);

            if (requestFundsTransaction.getRequester().equals(responderAccountInfo.getDocumentId().substring(5))) {
                throw new InvalidRequesteeException("Error: Unable to request money from yourself.");
            }
            if (requestFundsTransaction.getAmount() <= 0.0) throw new InvalidTransactionException("Amount must be a positive value.");

            List<AccountInfo> updatedAccounts = new ArrayList<>();

            List<Request> requesterRequests = requesterAccountInfo.getRequestHistory();
            List<Request> responderRequests = responderAccountInfo.getRequestHistory();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String currentTime = LocalDateTime.now().format(formatter);
            requestFundsTransaction.setDate(currentTime);


            // RequestStatus for the responder so they see PENDING for a request in their requests list
            requestFundsTransaction.setRequestStatus(Status.PENDING);

            responderRequests.add(requestFundsTransaction);
            responderAccountInfo.setRequestHistory(responderRequests);

            // RequestStatus for the requester so they see SENT for their request
            Request requesterRequest = new Request(requestFundsTransaction);
            requesterRequest.setRequestStatus(Status.SENT);

            requesterRequests.add(requesterRequest);
            requesterAccountInfo.setRequestHistory(requesterRequests);

            // Adding the requester and responder account infos into the updated accounts list to return
            updatedAccounts.add(requesterAccountInfo);
            updatedAccounts.add(responderAccountInfo);

            // Looping for each AccountInfo object in the updatedAccounts list and update the accounts on the database.
            for (AccountInfo account: updatedAccounts) {
                accountService.updateAccountInfo(account);
            }
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

        return new ResponseEntity<>("Successfully sent " + responder + " a request for $" + requestFundsTransaction.getAmount(), HttpStatus.CREATED);
    }

    public ResponseEntity<String> approveRequest(String id) throws Exception {
        AccountInfo responderAccountInfo = accountService.getAccountInfo(authenticationService.getCurrentUser());
        Buddy responderBuddies = accountService.getBuddy(authenticationService.getCurrentUser());
        Profile responderProfile = accountService.getProfile(authenticationService.getCurrentUser());
        String responderFullName = responderProfile.getFirstName() + " " + responderProfile.getLastName();
        List<Request> responderRequests = responderAccountInfo.getRequestHistory();

        Request parsed = new Request(id);

        // Loop through each request in responderRequest and locate the correct request object by requestId
        for (Request request: responderRequests) {
            if (request.getId().equals(parsed.getId())){
                String requester = "user_" + request.getRequester();
                AccountInfo requesterAccountInfo = accountService.getAccountInfo(requester);
                Buddy requesterBuddies = accountService.getBuddy(requester);
                Profile requesterProfile = accountService.getProfile(requester);
                String requesterFullName = requesterProfile.getFirstName() + " " + requesterProfile.getLastName();

                List<Object> updatedAccounts = new ArrayList<>();
                List<Object> updatedBuddies = new ArrayList<>();

                List<Transaction> responderTransactions = responderAccountInfo.getTransactionHistory();
                List<Transaction> requesterTransactions = requesterAccountInfo.getTransactionHistory();

                List<Request> requesterRequests = requesterAccountInfo.getRequestHistory();

                double responderBalance = responderAccountInfo.getBalance();
                double requesterBalance = requesterAccountInfo.getBalance();

                // We check if the person approving is equal to the responder of the request
                if (responderAccountInfo.getDocumentId().substring(5).equals(request.getResponder())) {
                    if (responderBalance - request.getAmount() < 0.0) {
                        throw new InsufficientFundsException(String.format("Insufficient funds. Current balance is $%.2f", responderBalance));
                    }
                    // We want to delete the old request information from both the requester and the responder
                    responderRequests.remove(request);
                    requesterRequests.removeIf(requesterRequest -> requesterRequest.getId().equals(request.getId()));

                    request.setRequestStatus(Status.APPROVED);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                    String currentTime = LocalDateTime.now().format(formatter);
                    request.setDate(currentTime);

                    Transaction approvedTransaction =
                            new Transaction(
                                    UUID.randomUUID().toString(),
                                    request.getMemo(),
                                    request.getResponder(),
                                    request.getRequester(),
                                    request.getAmount(),
                                    request.getDate(),
                                    TransactionType.REQUEST,
                                    Status.SENT,
                                    request.getPrivacySetting(),
                                    responderFullName,
                                    requesterFullName
                            );

                    responderTransactions.add(approvedTransaction);
                    responderRequests.add(request);
                    responderBalance -= request.getAmount();

                    // Updating the senders account
                    responderAccountInfo.setTransactionHistory(responderTransactions);
                    responderAccountInfo.setRequestHistory(responderRequests);
                    responderAccountInfo.setBalance(responderBalance);

                    // New Transaction object for the requester so we can change status.
                    Transaction requesterTransaction = new Transaction(approvedTransaction);
                    requesterTransaction.setTransactionStatus(Status.RECEIVED);

                    requesterTransactions.add(requesterTransaction);
                    requesterRequests.add(request);
                    requesterBalance += request.getAmount();

                    // Updating the requesters account
                    requesterAccountInfo.setTransactionHistory(requesterTransactions);
                    requesterAccountInfo.setRequestHistory(requesterRequests);
                    requesterAccountInfo.setBalance(requesterBalance);
                } else {
                    throw new InvalidRequesteeException("Error: Unable to send yourself money.");
                }

                // Adding the sender and recipient account infos into the updated accounts list to return
                updatedAccounts.add(responderAccountInfo);
                updatedAccounts.add(requesterAccountInfo);

                List<Transaction> responderBuddyTransactions = responderBuddies.getBuddyTransactions();
                List<Transaction> requesterBuddyTransactions = requesterBuddies.getBuddyTransactions();

                // We check if the person approving is equal to the responder of the request
                if (responderAccountInfo.getDocumentId().substring(5).equals(request.getResponder())) {

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                    String currentTime = LocalDateTime.now().format(formatter);
                    request.setDate(currentTime);

                    Transaction approvedTransaction =
                            new Transaction(
                                    UUID.randomUUID().toString(),
                                    request.getMemo(),
                                    request.getResponder(),
                                    request.getRequester(),
                                    request.getAmount(),
                                    request.getDate(),
                                    TransactionType.REQUEST,
                                    Status.SENT,
                                    request.getPrivacySetting(),
                                    responderFullName,
                                    requesterFullName
                            );

                    responderBuddyTransactions.add(approvedTransaction);

                    // Updating the senders buddies
                    responderBuddies.setBuddyTransactions(responderBuddyTransactions);

                    // New Transaction object for the requester so we can change status.
                    Transaction requesterTransaction = new Transaction(approvedTransaction);
                    requesterTransaction.setTransactionStatus(Status.RECEIVED);

                    requesterBuddyTransactions.add(requesterTransaction);

                    // Updating the requesters account
                    requesterBuddies.setBuddyTransactions(requesterBuddyTransactions);
                } else {
                    throw new InvalidRequesteeException("Error: Unable to send yourself money.");
                }

                // Adding the sender and recipient buddies into the updated accounts list to return
                updatedBuddies.add(responderBuddies);
                updatedBuddies.add(requesterBuddies);


                // Looping for each AccountInfo object in the updatedAccounts list and update the accounts on the database.
                for (Object account: updatedAccounts) {
                    accountService.updateAccountInfo((AccountInfo) account);
                }
                for (Object buddies: updatedBuddies) {
                    accountService.updateBuddy((Buddy) buddies);
                }

                return ResponseEntity.ok("Sent " + requester.substring(5) + " $" + request.getAmount());
            }
        }
        return ResponseEntity.badRequest().body("There was an error approving this request.");
    }

    public ResponseEntity<String> denyRequest(String id) throws Exception {
        AccountInfo responderAccountInfo = accountService.getAccountInfo(authenticationService.getCurrentUser());
        List<Request> responderRequests = responderAccountInfo.getRequestHistory();

        Request parsed = new Request(id);

        for (Request request: responderRequests) {
            if (request.getId().equals(parsed.getId())){
                String requester = "user_" + request.getRequester();
                AccountInfo requesterAccountInfo = accountService.getAccountInfo(requester);

                List<AccountInfo> updatedAccounts = new ArrayList<>();

                List<Request> requesterRequests = requesterAccountInfo.getRequestHistory();

                // We check if the person denying is equal to the original responder of the request
                if (responderAccountInfo.getDocumentId().substring(5).equals(request.getResponder())) {
                    responderRequests.remove(request);
                    requesterRequests.removeIf(requesterRequest -> requesterRequest.getId().equals(request.getId()));

                    // We updated the time and status for when the request was denied.
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                    String currentTime = LocalDateTime.now().format(formatter);
                    request.setDate(currentTime);
                    request.setRequestStatus(Status.DENIED);

                    // Adding the new request to requests list and updating the account info with the updated requests list
                    requesterRequests.add(request);
                    requesterAccountInfo.setRequestHistory(requesterRequests);

                    responderRequests.add(request);
                    responderAccountInfo.setRequestHistory(responderRequests);
                } else {
                    throw new InvalidTransactionException("Invalid or missing transaction type");
                }

                // Adding the requester and responder account infos into the updated accounts list to return
                updatedAccounts.add(responderAccountInfo);
                updatedAccounts.add(requesterAccountInfo);

                // Looping for each AccountInfo object in the updatedAccounts list and update the accounts on the database.
                for (AccountInfo account: updatedAccounts) {
                    accountService.updateAccountInfo(account);
                }

                return ResponseEntity.ok("Denied request from " + requester.substring(5) + " for $" + request.getAmount());
            }
        }

        return ResponseEntity.badRequest().body("There was an error denying this request.");
    }
}
