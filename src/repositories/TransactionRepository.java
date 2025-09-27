package repositories;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import models.Account;
import models.Transaction;
import exceptions.AccountNotFoundException;
import exceptions.InvalidTransactionException;

public class TransactionRepository implements RepositoryBase<Transaction> {
    private static TransactionRepository instance;
    private ArrayList<Transaction> transactions;

    private TransactionRepository() {
        transactions = new ArrayList<>();
    }

    public static TransactionRepository getInstance() {
        if (instance == null) {
            instance = new TransactionRepository();
        }
        return instance;
    }

    @Override
    public ArrayList<Transaction> all() {
        return transactions;
    }

    @Override
    public Optional<Transaction> findById(String value) {
        return transactions.stream()
                .filter(t -> t.getId().toString().equals(value))
                .findFirst();
    }

    public ArrayList<Transaction> findTransactionsByAccount(Account a) {
        return transactions.stream()
                .filter(t -> t.getSourceAccount().equals(a) || t.getDestinationAccount().equals(a))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void save(Transaction transaction) {
        if (transaction == null) {
            throw new InvalidTransactionException("Cannot save null transaction");
        }
        if (transaction.getSourceAccount() == null) {
            throw new AccountNotFoundException("Source account cannot be null");
        }
        if (transaction.getDestinationAccount() == null) {
            throw new AccountNotFoundException("Destination account cannot be null");
        }
        if (transaction.getAmount() <= 0) {
            throw new exceptions.NegativeAmountException(transaction.getAmount());
        }

        transaction.getSourceAccount().addTransaction(transaction);
        if (!transaction.getSourceAccount().equals(transaction.getDestinationAccount())) {
            transaction.getDestinationAccount().addTransaction(transaction);
        }
        transactions.add(transaction);
    }

    @Override
    public void delete(String field, String value) {
        switch (field.toLowerCase()) {
            case "id":
                transactions.removeIf(t -> t.getId().toString().equals(value));
                break;
            case "transactiontype":
                transactions.removeIf(t -> t.getTransactionType().toString().equalsIgnoreCase(value));
                break;
            case "description":
                transactions.removeIf(t -> t.getDescription().equalsIgnoreCase(value));
                break;
            case "date":
                transactions.removeIf(t -> t.getDate().toString().equalsIgnoreCase(value));
                break;
            case "amount":
                transactions.removeIf(t -> Double.toString(t.getAmount()).equalsIgnoreCase(value));
                break;
            default:
                break;
        }
    }

    @Override
    public void delete(Transaction t) {
        if (t == null) {
            throw new InvalidTransactionException("Cannot delete null transaction");
        }
        if (!transactions.contains(t)) {
            throw new InvalidTransactionException("Transaction not found in repository");
        }
        transactions.remove(t);
    }

}
