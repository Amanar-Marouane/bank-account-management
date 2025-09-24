package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import exceptions.InvalidTransactionException;
import exceptions.InsufficientFundsException;
import exceptions.NegativeAmountException;

public class Account {
    private UUID id;
    private AccountType accountType;
    private double balance;
    private List<Transaction> transactions;
    private Customer customer;

    public Account(AccountType accountType, Customer customer) {
        this.id = UUID.randomUUID();
        this.accountType = accountType;
        this.customer = customer;
        this.transactions = new ArrayList<>();
        this.balance = 0;
    }

    public UUID getId() {
        return id;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public double getBalance() {
        return balance;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new InvalidTransactionException("transaction", "Transaction cannot be null");
        }
        if (transaction.getAmount() <= 0) {
            throw new NegativeAmountException(transaction.getAmount());
        }

        transactions.add(transaction);

        // Update balance based on transaction type
        switch (transaction.getTransactionType()) {
            case DEPOSIT:
                balance += transaction.getAmount();
                break;
            case WITHDRAWAL:
                if (balance < transaction.getAmount()) {
                    throw new InsufficientFundsException(transaction.getAmount(), balance);
                }
                balance -= transaction.getAmount();
                break;
            case TRANSFER:
                if (transaction.getSourceAccount() == this) {
                    if (balance < transaction.getAmount()) {
                        throw new InsufficientFundsException(transaction.getAmount(), balance);
                    }
                    balance -= transaction.getAmount();
                }
                if (transaction.getDestinationAccount() == this) {
                    balance += transaction.getAmount();
                }
                break;
            default:
                throw new InvalidTransactionException("transaction",
                        "Unknown transaction type: " + transaction.getTransactionType());
        }
    }
}
