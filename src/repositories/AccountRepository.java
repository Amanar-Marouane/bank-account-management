package repositories;

import java.util.ArrayList;
import java.util.Optional;

import models.Account;
import exceptions.AccountNotFoundException;
import exceptions.CustomerNotFoundException;
import exceptions.InvalidTransactionException;

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
    public Optional<Account> findById(String value) {
        return accounts.stream()
                .filter(a -> a.getId().toString().equals(value))
                .findFirst();
    }

    public Optional<Account> findAccountsByCustomer(Object customer) {
        return accounts.stream()
                .filter(a -> a.getCustomer().equals(customer))
                .findFirst();
    }

    @Override
    public void save(Account account) {
        if (account == null) {
            throw new AccountNotFoundException("Cannot save null account");
        }
        if (account.getCustomer() == null) {
            throw new CustomerNotFoundException("Account must have a customer");
        }

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
        if (a == null) {
            throw new AccountNotFoundException("Cannot delete null account");
        }
        if (!accounts.contains(a)) {
            throw new AccountNotFoundException("Account not found in repository");
        }
        if (!a.getTransactions().isEmpty()) {
            throw new InvalidTransactionException("deletion", "Cannot delete account with existing transactions");
        }

        accounts.remove(a);
    }

}
