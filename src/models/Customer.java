package models;

import java.util.ArrayList;
import java.util.List;

public class Customer extends Person {
    private List<Account> accounts;

    public Customer(String firstName, String lastName, String email, String password, UserType userType) {
        super(firstName, lastName, email, password, userType);
        this.accounts = new ArrayList<>();
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    // Setter methods for admin updates
    public void setFirstName(String firstName) {
        super.setFirstName(firstName);
    }

    public void setLastName(String lastName) {
        super.setLastName(lastName);
    }

    public void setEmail(String email) {
        super.setEmail(email);
    }

    public void setUserType(UserType userType) {
        super.setUserType(userType);
    }
}
