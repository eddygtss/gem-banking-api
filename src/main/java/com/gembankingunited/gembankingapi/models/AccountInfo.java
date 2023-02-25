package com.gembankingunited.gembankingapi.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter @Setter
@RequiredArgsConstructor
public class AccountInfo {
    private String documentId;
    private String accountName;
    private double balance;
    private List<Transaction> transactionHistory;
    private List<Request> requestHistory;

    public AccountInfo(String documentId, String accountName, double balance, List<Transaction> transactions, List<Request> requests) {
        this.documentId = documentId;
        this.accountName = accountName;
        this.balance = balance;
        this.transactionHistory = transactions;
        this.requestHistory = requests;
    }

    // return immutable array of transactions
    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }
    public List<Request> getRequestHistory() {
        return requestHistory;
    }
}
