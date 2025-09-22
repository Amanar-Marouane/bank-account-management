package app.repositories;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import app.interfaces.RepositoryBase;
import app.models.Account;
import app.models.Transaction;

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
    public Optional<Transaction> find(String field, String value) {
        switch (field.toLowerCase()) {
            case "id":
                return transactions.stream()
                        .filter(t -> t.getId().toString().equals(value))
                        .findFirst();
            case "transactiontype":
                return transactions.stream()
                        .filter(t -> t.getTransactionType().toString().equalsIgnoreCase(value))
                        .findFirst();
            case "description":
                return transactions.stream()
                        .filter(t -> t.getDescription().equalsIgnoreCase(value))
                        .findFirst();
            case "data":
                return transactions.stream()
                        .filter(t -> t.getDate().toString().equalsIgnoreCase(value))
                        .findFirst();
            case "amount":
                return transactions.stream()
                        .filter(t -> Double.toString(t.getAmount()).equalsIgnoreCase(value))
                        .findFirst();
            default:
                return Optional.empty();
        }
    }

    public ArrayList<Transaction> findTransactionsByAccount(Account a) {
        return transactions.stream()
                .filter(t -> t.getSourceAccount().equals(a) || t.getDestinationAccount().equals(a))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void save(Transaction transaction) {
        transaction.getSourceAccount().addTransaction(transaction);
        transaction.getDestinationAccount().addTransaction(transaction);
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
        transactions.remove(t);
    }

}
