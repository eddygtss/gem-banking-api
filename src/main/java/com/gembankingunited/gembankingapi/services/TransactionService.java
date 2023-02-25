package com.gembankingunited.gembankingapi.services;

import com.gembankingunited.gembankingapi.enums.Status;
import com.gembankingunited.gembankingapi.enums.TransactionType;
import com.gembankingunited.gembankingapi.exceptions.InsufficientFundsException;
import com.gembankingunited.gembankingapi.exceptions.InvalidTransactionException;
import com.gembankingunited.gembankingapi.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TransactionService {
    @Autowired
    public AccountService accountService;
    @Autowired
    public AuthenticationService authenticationService;

    public List<Transaction> retrieveTransactions() throws Exception {
        AccountInfo accountInfo = accountService.getAccountInfo(authenticationService.getCurrentUser());

        return accountInfo.getTransactionHistory();
    }

    public ResponseEntity<Void> recordTransaction(Transaction transaction) throws Exception {
        AccountInfo accountInfo = accountService.getAccountInfo(authenticationService.getCurrentUser());

        List<Transaction> transactions = accountInfo.getTransactionHistory();
        double balance = accountInfo.getBalance();
        TransactionType transactionType = transaction.getTransactionType();

        if (transaction.getAmount() <= 0.0) throw new InvalidTransactionException("Amount must be a positive value.");


        if (transaction.getDate() == null || transaction.getDate().equals("")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String currentTime = LocalDateTime.now().format(formatter);
            transaction.setDate(currentTime);
        }

        if (transactionType == TransactionType.DEPOSIT) {
            transaction.setTransactionStatus(Status.PROCESSED);
            transactions.add(transaction);
            balance += transaction.getAmount();
        } else if (transactionType == TransactionType.WITHDRAWAL) {
            if (balance - transaction.getAmount() < 0.0) {
                throw new InsufficientFundsException(String.format("Insufficient funds. Current balance is $%.2f", balance));
            }
            transaction.setTransactionStatus(Status.PROCESSED);
            transactions.add(transaction);
            balance -= transaction.getAmount();
        } else {
            throw new InvalidTransactionException("Invalid or missing transaction type");
        }

        accountInfo.setTransactionHistory(transactions);
        accountInfo.setBalance(balance);

        accountService.updateAccountInfo(accountInfo);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    public ResponseEntity<String> sendTransaction(Transaction transaction) throws Exception {
        String sender = authenticationService.getCurrentUser();
        String recipient = transaction.getRecipient().toLowerCase();
        transaction.setRecipient(recipient);
        Profile senderProfile = accountService.getProfile(authenticationService.getCurrentUser());
        String senderFullName = senderProfile.getFirstName() + " " + senderProfile.getLastName();
        transaction.setSenderFullName(senderFullName);

        if (sender.substring(5).equals(transaction.getRecipient())){
            return ResponseEntity.badRequest().body("You cannot send money to yourself.");
        }

        AccountInfo senderAccountInfo = accountService.getAccountInfo(sender);
        Buddy senderBuddies = accountService.getBuddy(sender);
        transaction.setSender(sender.substring(5));

        try {
            AccountInfo recipientAccountInfo = accountService.getAccountInfo("user_" + recipient);
            Buddy recipientBuddies = accountService.getBuddy("user_" + recipient);
            Profile recipientProfile = accountService.getProfile("user_" + recipient);
            String recipientFullName = recipientProfile.getFirstName() + " " + recipientProfile.getLastName();
            transaction.setRecipientFullName(recipientFullName);

            List<AccountInfo> updatedAccounts = new ArrayList<>();

            List<Transaction> senderTransactions = senderAccountInfo.getTransactionHistory();
            List<Transaction> recipientTransactions = recipientAccountInfo.getTransactionHistory();

            double senderBalance = senderAccountInfo.getBalance();
            double recipientBalance = recipientAccountInfo.getBalance();

            // TransactionType for the sender
            TransactionType transactionType = transaction.getTransactionType();

            if (transaction.getAmount() <= 0.0) throw new InvalidTransactionException("Amount must be a positive value.");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String currentTime = LocalDateTime.now().format(formatter);
            transaction.setDate(currentTime);

            // We check if the transaction type is SEND because we are subtracting money from the account balance
            if (transactionType == TransactionType.SEND) {
                if (senderBalance - transaction.getAmount() < 0.0) {
                    throw new InsufficientFundsException(String.format("Insufficient funds. Current balance is $%.2f", senderBalance));
                }
                transaction.setTransactionStatus(Status.SENT);
                senderTransactions.add(transaction);
                senderBalance -= transaction.getAmount();

                // Updating the sender's account
                senderAccountInfo.setTransactionHistory(senderTransactions);
                senderAccountInfo.setBalance(senderBalance);

                // New Transaction object for the recipient so we can change status and type.
                Transaction recipientTransaction = new Transaction(transaction);
                recipientTransaction.setTransactionStatus(Status.RECEIVED);
                recipientTransaction.setTransactionType(TransactionType.TRANSFER);

                recipientTransactions.add(recipientTransaction);
                recipientBalance += transaction.getAmount();

                // Updating the recipient's account
                recipientAccountInfo.setTransactionHistory(recipientTransactions);
                recipientAccountInfo.setBalance(recipientBalance);
            } else {
                throw new InvalidTransactionException("Invalid or missing transaction type");
            }

            // Adding the sender and recipient account infos into the updated accounts list to return
            updatedAccounts.add(senderAccountInfo);
            updatedAccounts.add(recipientAccountInfo);

            // Looping for each AccountInfo object in the updatedAccounts list and update the accounts on the database.
            for (AccountInfo account: updatedAccounts) {
                accountService.updateAccountInfo(account);
            }

            List<Buddy> updatedBuddies = new ArrayList<>();

            List<Transaction> senderBuddyTransactions = senderBuddies.getBuddyTransactions();
            List<Transaction> recipientBuddyTransactions = recipientBuddies.getBuddyTransactions();

            // We check if the transaction type is SEND because we are subtracting money from the account balance
            transaction.setTransactionStatus(Status.SENT);
            senderBuddyTransactions.add(transaction);

            // Updating the sender's account
            senderBuddies.setBuddyTransactions(senderBuddyTransactions);

            // New Transaction object for the recipient, so we can change status and type.
            Transaction recipientTransaction = new Transaction(transaction);
            recipientTransaction.setTransactionStatus(Status.RECEIVED);
            recipientTransaction.setTransactionType(TransactionType.TRANSFER);

            recipientBuddyTransactions.add(recipientTransaction);

            // Updating the recipient's account
            recipientBuddies.setBuddyTransactions(recipientBuddyTransactions);

            // Adding the sender and recipient account infos into the updated accounts list to return
            updatedBuddies.add(senderBuddies);
            updatedBuddies.add(recipientBuddies);

            for (Buddy buddy: updatedBuddies) {
                accountService.updateBuddy(buddy);
            }

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

        return new ResponseEntity<>("Successfully sent " + recipient + " $" + transaction.getAmount(), HttpStatus.CREATED);
    }
}
