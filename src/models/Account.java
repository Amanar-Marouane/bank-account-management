package models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        transactions.add(transaction);
        // Update balance based on transaction type
        switch (transaction.getTransactionType()) {
            case DEPOSIT:
                balance += transaction.getAmount();
                break;
            case WITHDRAWAL:
                balance -= transaction.getAmount();
                break;
            case TRANSFER:
                if (transaction.getSourceAccount() == this)
                    balance -= transaction.getAmount();
                if (transaction.getDestinationAccount() == this)
                    balance += transaction.getAmount();
                break;
        }
    }
}
