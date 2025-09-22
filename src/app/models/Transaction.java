package app.models;

import java.util.Date;
import java.util.UUID;

import app.enums.TransactionType;

public class Transaction {
    private UUID id;
    private TransactionType transactionType;
    private double amount;
    private Date date;
    private String description;
    private Account sourceAccount;
    private Account destinationAccount;

    public Transaction(
            TransactionType transactionType,
            double amount,
            String description,
            Account sourceAccount,
            Account destinationAccount) {
        this.id = UUID.randomUUID();
        this.transactionType = transactionType;
        this.amount = amount;
        this.date = new Date();
        this.description = description;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
    }

    public UUID getId() {
        return id;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public Account getDestinationAccount() {
        return destinationAccount;
    }
}
