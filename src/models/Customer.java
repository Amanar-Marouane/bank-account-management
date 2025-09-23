package models;

import java.util.ArrayList;
import java.util.List;

public class Customer extends Person {
    private List<Account> accounts;

    public Customer(String firstName, String lastName, String email, String password) {
        super(firstName, lastName, email, password);
        this.accounts = new ArrayList<>();
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }
}
