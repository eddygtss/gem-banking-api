package com.gembankingunited.gembankingapi.controllers;

import com.gembankingunited.gembankingapi.models.Transaction;
import com.gembankingunited.gembankingapi.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin("http://localhost:3000")
@RequestMapping("/api/v1")
public class TransactionController {
    public TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // This controller defines all of our API endpoints for Transactions.
    // Get Transactions API endpoint (GET/Read)
    @GetMapping("/transactions")
    public List<Transaction> retrieveTransactionHistory() {
        return transactionService.retrieveTransactions();
    }

    // Post (DEPOSIT/WITHDRAWAL) Transactions API endpoint (POST/Create)
    @PostMapping("/transactions")
    public ResponseEntity<Void> createTransaction(@RequestBody Transaction createTransactionRequest) {
        return transactionService.recordTransaction(createTransactionRequest);
    }

    // Send Transaction API endpoint (POST/Create)
    @PostMapping("/send")
    public ResponseEntity<String> sendFunds(@RequestBody Transaction sendFundsTransaction) {
        return transactionService.sendTransaction(sendFundsTransaction);
    }
}
