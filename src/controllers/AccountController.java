package controllers;

import models.Account;
import models.AccountType;
import models.Customer;
import repositories.AccountRepository;
import services.AuthInterface;
import utils.Console;

import java.util.Optional;

public final class AccountController {
    private AuthInterface auth;
    private AccountRepository accountRepository;

    public AccountController(AuthInterface auth) {
        this.auth = auth;
        this.accountRepository = AccountRepository.getInstance();
    }

    // Only Admins can create accounts
    public void store() {
        // Check if user is authenticated
        if (!auth.isAuthenticated()) {
            Console.error("You must be logged in to create an account.");
            return;
        }

        Optional<Customer> currentCustomer = auth.getCurrentUser();

        if (!currentCustomer.isPresent()) {
            Console.error("User not found. Please log in again.");
            return;
        }

        Customer c = currentCustomer.get();

        Console.info(
                "Creating a new account for: " + c.getFullName());

        // Display available account types
        Console.info("Available account types:");
        AccountType[] accountTypes = AccountType.values();
        for (int i = 0; i < accountTypes.length; i++) {
            Console.info((i + 1) + ". " + accountTypes[i]);
        }

        // Get account type selection
        Console.print("Select account type (1-" + accountTypes.length + "): ");
        int choice;
        try {
            choice = Integer.parseInt(Console.ask(""));
            if (choice < 1 || choice > accountTypes.length) {
                Console.error("Invalid selection. Please choose a number between 1 and " + accountTypes.length);
                return;
            }
        } catch (NumberFormatException e) {
            Console.error("Invalid input. Please enter a number.");
            return;
        }

        AccountType selectedType = accountTypes[choice - 1];

        // Check if customer already has this type of account
        boolean hasAccountType = c.getAccounts().stream()
                .anyMatch(account -> account.getAccountType() == selectedType);

        if (hasAccountType) {
            Console.error("You already have a " + selectedType + " account.");
            return;
        }

        // Create and save the new account
        Account newAccount = new Account(selectedType, c);
        accountRepository.save(newAccount);

        Console.success("Account created successfully!");
        Console.info("Account ID: " + newAccount.getId());
        Console.info("Account Type: " + newAccount.getAccountType());
        Console.info("Initial Balance: $" + String.format("%.2f", newAccount.getBalance()));
    }
}
