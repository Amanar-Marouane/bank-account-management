package app.repositories;

import java.util.ArrayList;
import java.util.Optional;

import app.interfaces.RepositoryBase;
import app.models.Account;

public class AccountRepository implements RepositoryBase<Account> {
    private static AccountRepository instance;
    private ArrayList<Account> accounts;

    private AccountRepository() {
        this.accounts = new ArrayList<>();
    }

    public static AccountRepository getInstance() {
        if (instance == null) {
            instance = new AccountRepository();
        }
        return instance;
    }

    @Override
    public ArrayList<Account> all() {
        return accounts;
    }

    @Override
    public Optional<Account> find(String field, String value) {
        switch (field.toLowerCase()) {
            case "id":
                return accounts.stream()
                        .filter(a -> a.getId().toString().equals(value))
                        .findFirst();
            case "accountType":
                return accounts.stream()
                        .filter(a -> a.getAccountType().toString().equalsIgnoreCase(value))
                        .findFirst();
            case "balance":
                return accounts.stream()
                        .filter(a -> Double.toString(a.getBalance()).equalsIgnoreCase(value))
                        .findFirst();
            default:
                return Optional.empty();
        }
    }

    public Optional<Account> findAccountsByCustomer(Object customer) {
        return accounts.stream()
                .filter(a -> a.getCustomer().equals(customer))
                .findFirst();
    }

    @Override
    public void save(Account account) {
        account.getCustomer().addAccount(account);
        accounts.add(account);
    }

    @Override
    public void delete(String field, String value) {
        switch (field.toLowerCase()) {
            case "id":
                accounts.removeIf(a -> a.getId().toString().equals(value));
                break;
            case "accounttype":
                accounts.removeIf(a -> a.getAccountType().toString().equalsIgnoreCase(value));
                break;
            case "balance":
                accounts.removeIf(a -> a.getBalance() == Double.parseDouble(value));
                break;
            default:
                break;
        }
    }

    @Override
    public void delete(Account a) {
        accounts.remove(a);
    }

}
