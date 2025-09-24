package controllers;

import models.Account;
import models.AccountType;
import models.Customer;
import models.UserType;
import repositories.AccountRepository;
import repositories.CustomerRepository;
import services.AuthInterface;
import utils.Console;

import java.util.List;
import java.util.Optional;

public class AdminController {
    private AuthInterface auth;
    private CustomerRepository customerRepository;
    private AccountRepository accountRepository;

    public AdminController(AuthInterface auth) {
        this.auth = auth;
        this.customerRepository = CustomerRepository.getInstance();
        this.accountRepository = AccountRepository.getInstance();
    }

    public void manageCustomers() {
        while (true) {
            Console.line();
            Console.info("Customer Management");
            Console.info("1) List all customers");
            Console.info("2) Search customer");
            Console.info("3) Create new customer");
            Console.info("0) Back to main menu");
            Console.line();

            String choice = Console.ask("Enter choice: ");
            switch (choice) {
                case "0":
                    return;
                case "1":
                    listAllCustomers();
                    break;
                case "2":
                    searchCustomer();
                    break;
                case "3":
                    createCustomer();
                    break;
                default:
                    Console.error("Invalid option!");
                    break;
            }
        }
    }

    private void listAllCustomers() {
        List<Customer> customers = customerRepository.all();
        if (customers.isEmpty()) {
            Console.warning("No customers found.");
            return;
        }

        Console.line();
        Console.info("All Customers:");
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            Console.info((i + 1) + ") " + c.getFullName() + " (" + c.getEmail() + ") - " + c.getUserType());
        }
        Console.line();

        String choice = Console.ask("Enter customer number to manage (0 to go back): ");
        if (!choice.equals("0")) {
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < customers.size()) {
                    manageSpecificCustomer(customers.get(index));
                } else {
                    Console.error("Invalid customer number!");
                }
            } catch (NumberFormatException e) {
                Console.error("Invalid input!");
            }
        }
    }

    private void searchCustomer() {
        String email = Console.ask("Enter customer email to search: ");
        Optional<Customer> customer = customerRepository.find("email", email);

        if (customer.isPresent()) {
            manageSpecificCustomer(customer.get());
        } else {
            Console.error("Customer not found!");
        }
    }

    private void createCustomer() {
        String firstName = Console.ask("Enter first name: ");
        String lastName = Console.ask("Enter last name: ");
        String email = Console.ask("Enter email: ");
        String password = Console.ask("Enter password: ");

        Console.info("Select user type:");
        Console.info("1) USER");
        Console.info("2) ADMIN");
        String typeChoice = Console.ask("Enter choice (1-2): ");

        UserType userType = typeChoice.equals("2") ? UserType.ADMIN : UserType.USER;

        // Check if email already exists
        if (customerRepository.find("email", email).isPresent()) {
            Console.error("Email already exists!");
            return;
        }

        Customer newCustomer = new Customer(firstName, lastName, email, password, userType);
        customerRepository.save(newCustomer);
        Console.success("Customer created successfully!");
    }

    private void manageSpecificCustomer(Customer customer) {
        while (true) {
            Console.line();
            Console.info("Managing: " + customer.getFullName() + " (" + customer.getEmail() + ")");
            Console.info("1) View customer details");
            Console.info("2) Update customer");
            Console.info("3) Delete customer");

            // Only show account management for regular users, not admins
            if (customer.getUserType() == UserType.USER) {
                Console.info("4) Manage customer accounts");
            }

            Console.info("0) Back");
            Console.line();

            String choice = Console.ask("Enter choice: ");
            switch (choice) {
                case "0":
                    return;
                case "1":
                    viewCustomerDetails(customer);
                    break;
                case "2":
                    updateCustomer(customer);
                    break;
                case "3":
                    if (deleteCustomer(customer)) {
                        return; // Customer deleted, go back
                    }
                    break;
                case "4":
                    if (customer.getUserType() == UserType.USER) {
                        manageCustomerAccounts(customer);
                    } else {
                        Console.error("Invalid option!");
                    }
                    break;
                default:
                    Console.error("Invalid option!");
                    break;
            }
        }
    }

    private void viewCustomerDetails(Customer customer) {
        Console.line();
        Console.info("Customer Details:");
        Console.info("ID: " + customer.getId());
        Console.info("Name: " + customer.getFullName());
        Console.info("Email: " + customer.getEmail());
        Console.info("User Type: " + customer.getUserType());
        Console.info("Number of Accounts: " + customer.getAccounts().size());
        Console.line();
    }

    private void updateCustomer(Customer customer) {
        Console.line();
        Console.info("Update Customer:");
        Console.info("1) Update first name");
        Console.info("2) Update last name");
        Console.info("3) Update email");
        Console.info("4) Update user type");
        Console.info("0) Back");
        Console.line();

        String choice = Console.ask("Enter choice: ");
        switch (choice) {
            case "0":
                return;
            case "1":
                String newFirstName = Console.ask("Enter new first name: ");
                customer.setFirstName(newFirstName);
                Console.success("First name updated successfully!");
                break;
            case "2":
                String newLastName = Console.ask("Enter new last name: ");
                customer.setLastName(newLastName);
                Console.success("Last name updated successfully!");
                break;
            case "3":
                String newEmail = Console.ask("Enter new email: ");
                if (customerRepository.find("email", newEmail).isPresent()) {
                    Console.error("Email already exists!");
                } else {
                    customer.setEmail(newEmail);
                    Console.success("Email updated successfully!");
                }
                break;
            case "4":
                Console.info("Select new user type:");
                Console.info("1) USER");
                Console.info("2) ADMIN");
                String typeChoice = Console.ask("Enter choice (1-2): ");
                UserType newType = typeChoice.equals("2") ? UserType.ADMIN : UserType.USER;
                customer.setUserType(newType);
                Console.success("User type updated successfully!");
                break;
            default:
                Console.error("Invalid option!");
                break;
        }
    }

    private boolean deleteCustomer(Customer customer) {
        // Prevent admin from deleting themselves
        Customer currentUser = auth.getCurrentUser().orElse(null);
        if (currentUser != null && currentUser.getId().equals(customer.getId())) {
            Console.error("You cannot delete your own account!");
            return false;
        }

        if (Console.confirm("Are you sure you want to delete this customer?")) {
            customerRepository.delete(customer);
            Console.success("Customer deleted successfully!");
            return true;
        }
        return false;
    }

    private void manageCustomerAccounts(Customer customer) {
        if (customer.getUserType() == UserType.ADMIN) {
            Console.error("Account management is not available for admin users.");
            return;
        }

        while (true) {
            Console.line();
            Console.info("Managing Accounts for: " + customer.getFullName());
            Console.info("1) List customer accounts");
            Console.info("2) Create new account");
            Console.info("3) Delete account");
            Console.info("0) Back");
            Console.line();

            String choice = Console.ask("Enter choice: ");
            switch (choice) {
                case "0":
                    return;
                case "1":
                    listCustomerAccounts(customer);
                    break;
                case "2":
                    createAccountForCustomer(customer);
                    break;
                case "3":
                    deleteCustomerAccount(customer);
                    break;
                default:
                    Console.error("Invalid option!");
                    break;
            }
        }
    }

    private void listCustomerAccounts(Customer customer) {
        if (customer.getUserType() == UserType.ADMIN) {
            Console.warning("Admin users do not have accounts.");
            return;
        }

        List<Account> accounts = customer.getAccounts();
        if (accounts.isEmpty()) {
            Console.warning("No accounts found for this customer.");
            return;
        }

        Console.line();
        Console.info("Accounts for " + customer.getFullName() + ":");
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            Console.info((i + 1) + ") " + account.getAccountType() +
                    " - Balance: $" + String.format("%.2f", account.getBalance()) +
                    " - ID: " + account.getId());
        }
        Console.line();
    }

    private void createAccountForCustomer(Customer customer) {
        if (customer.getUserType() == UserType.ADMIN) {
            Console.error("Cannot create accounts for admin users.");
            return;
        }

        Console.info("Available account types:");
        AccountType[] accountTypes = AccountType.values();
        for (int i = 0; i < accountTypes.length; i++) {
            Console.info((i + 1) + ". " + accountTypes[i]);
        }

        String choice = Console.ask("Select account type (1-" + accountTypes.length + "): ");
        try {
            int typeIndex = Integer.parseInt(choice) - 1;
            if (typeIndex >= 0 && typeIndex < accountTypes.length) {
                AccountType selectedType = accountTypes[typeIndex];

                // Check if customer already has this type of account
                boolean hasAccountType = customer.getAccounts().stream()
                        .anyMatch(account -> account.getAccountType() == selectedType);

                if (hasAccountType) {
                    Console.error("Customer already has a " + selectedType + " account.");
                    return;
                }

                Account newAccount = new Account(selectedType, customer);
                accountRepository.save(newAccount);
                Console.success("Account created successfully!");
                Console.info("Account ID: " + newAccount.getId());
            } else {
                Console.error("Invalid selection!");
            }
        } catch (NumberFormatException e) {
            Console.error("Invalid input!");
        }
    }

    private void deleteCustomerAccount(Customer customer) {
        // Prevent account deletion for admin users
        if (customer.getUserType() == UserType.ADMIN) {
            Console.error("Admin users do not have accounts to delete.");
            return;
        }

        List<Account> accounts = customer.getAccounts();
        if (accounts.isEmpty()) {
            Console.warning("No accounts found for this customer.");
            return;
        }

        Console.line();
        Console.info("Select account to delete:");
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            Console.info((i + 1) + ") " + account.getAccountType() +
                    " - Balance: $" + String.format("%.2f", account.getBalance()));
        }
        Console.line();

        String choice = Console.ask("Enter account number to delete (0 to cancel): ");
        if (!choice.equals("0")) {
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < accounts.size()) {
                    Account accountToDelete = accounts.get(index);
                    if (Console.confirm("Are you sure you want to delete this account?")) {
                        accountRepository.delete(accountToDelete);
                        customer.getAccounts().remove(accountToDelete);
                        Console.success("Account deleted successfully!");
                    }
                } else {
                    Console.error("Invalid account number!");
                }
            } catch (NumberFormatException e) {
                Console.error("Invalid input!");
            }
        }
    }
}
