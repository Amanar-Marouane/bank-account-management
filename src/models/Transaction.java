package models;

import java.time.LocalDate;
import java.util.UUID;
import exceptions.AccountNotFoundException;
import exceptions.InvalidTransactionException;
import exceptions.NegativeAmountException;

public class Transaction {
    private UUID id;
    private TransactionType transactionType;
    private double amount;
    private LocalDate date;
    private String description;
    private Account sourceAccount;
    private Account destinationAccount;

    public Transaction(
            TransactionType transactionType,
            double amount,
            String description,
            Account sourceAccount,
            Account destinationAccount) {

        // Validation
        if (transactionType == null) {
            throw new InvalidTransactionException("creation", "Transaction type cannot be null");
        }
        if (amount <= 0) {
            throw new NegativeAmountException(amount);
        }
        if (sourceAccount == null) {
            throw new AccountNotFoundException("Source account cannot be null");
        }
        if (destinationAccount == null) {
            throw new AccountNotFoundException("Destination account cannot be null");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidTransactionException("creation", "Description cannot be null or empty");
        }
        if (transactionType == TransactionType.TRANSFER && sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new InvalidTransactionException("transfer", "Cannot transfer to the same account");
        }

        this.id = UUID.randomUUID();
        this.transactionType = transactionType;
        this.amount = amount;
        this.date = LocalDate.now();
        this.description = description.trim();
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

    public LocalDate getDate() {
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
