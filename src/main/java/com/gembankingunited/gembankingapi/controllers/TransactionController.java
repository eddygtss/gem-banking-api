package com.gembankingunited.gembankingapi.controllers;

import com.gembankingunited.gembankingapi.models.Transaction;
import com.gembankingunited.gembankingapi.services.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class TransactionController {
    @Autowired
    public TransactionService transactionService;

    // This controller defines all of our API endpoints for Transactions.
    // Get Transactions API endpoint (GET/Read)
    @GetMapping("/transactions")
    public List<Transaction> retrieveTransactionHistory() throws Exception  {
        return transactionService.retrieveTransactions();
    }

    // Post (DEPOSIT/WITHDRAWAL) Transactions API endpoint (POST/Create)
    @PostMapping("/transactions")
    public ResponseEntity<Void> createTransaction(@RequestBody Transaction createTransactionRequest) throws Exception {
        return transactionService.recordTransaction(createTransactionRequest);
    }

    // Send Transaction API endpoint (POST/Create)
    @PostMapping("/send")
    public ResponseEntity<String> sendFunds(@RequestBody Transaction sendFundsTransaction) throws Exception {
        return transactionService.sendTransaction(sendFundsTransaction);
    }
}
