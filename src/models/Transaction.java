package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import exceptions.AccountNotFoundException;
import exceptions.InvalidTransactionException;
import exceptions.NegativeAmountException;

public class Transaction {
    private UUID id;
    private TransactionType transactionType;
    private double amount;
    private LocalDateTime dateTime;
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
        this.dateTime = LocalDateTime.now();
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    // Keep getDate() for backward compatibility, returns LocalDate part
    public java.time.LocalDate getDate() {
        return dateTime.toLocalDate();
    }

    // Get formatted date and time string
    public String getFormattedDateTime() {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Get formatted date only
    public String getFormattedDate() {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    // Get formatted time only
    public String getFormattedTime() {
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
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
