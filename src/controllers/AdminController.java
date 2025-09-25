package controllers;

import models.Account;
import models.AccountType;
import models.Customer;
import models.Transaction;
import models.TransactionType;
import models.UserType;
import repositories.AccountRepository;
import repositories.CustomerRepository;
import repositories.TransactionRepository;
import services.AuthInterface;
import services.FilterService;
import services.StatisticsService;
import utils.Console;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AdminController {
    private final AuthInterface auth;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionController transactionController;
    private final TransactionRepository transactionRepository;
    private final CustomerController customerController;
    private final FilterService filterService;
    private final StatisticsService statisticsService;

    public AdminController(AuthInterface auth) {
        this.auth = auth;
        this.customerRepository = CustomerRepository.getInstance();
        this.accountRepository = AccountRepository.getInstance();
        this.transactionController = new TransactionController();
        this.transactionRepository = TransactionRepository.getInstance();
        this.customerController = new CustomerController(auth);
        this.filterService = FilterService.getInstance();
        this.statisticsService = StatisticsService.getInstance();
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
                    customerController.createCustomer();
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

    private void manageSpecificCustomer(Customer customer) {
        while (true) {
            Console.line();
            Console.info("Managing: " + customer.getFullName() + " (" + customer.getEmail() + ")");
            Console.info("1) View customer details");
            Console.info("2) Update customer");
            Console.info("3) Delete customer");
            Console.info("4) View customer transactions");

            // Only show account management for regular users, not admins
            if (customer.getUserType() == UserType.USER) {
                Console.info("5) Manage customer accounts");
            }

            Console.info("0) Back");
            Console.line();

            String choice = Console.ask("Enter choice: ");
            switch (choice) {
                case "0":
                    return;
                case "1":
                    customerController.profile(customer, "Customer Details");
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
                    viewCustomerTransactionsAdmin(customer);
                    break;
                case "5":
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
            Console.info("4) Manage account transactions (Admin)");
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
                case "4":
                    manageAdminAccountTransactions(customer);
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

    private void manageAdminAccountTransactions(Customer customer) {
        List<Account> accounts = customer.getAccounts();
        if (accounts.isEmpty()) {
            Console.warning("No accounts found for this customer.");
            return;
        }

        Console.line();
        Console.info("Admin - Select account to manage transactions:");
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            Console.info((i + 1) + ") " + account.getAccountType() +
                    " - Balance: $" + String.format("%.2f", account.getBalance()) +
                    " - ID: " + account.getId());
        }
        Console.line();

        String choice = Console.ask("Enter account number (0 to cancel): ");
        if (!choice.equals("0")) {
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < accounts.size()) {
                    Account selectedAccount = accounts.get(index);
                    showTransactionMenu(selectedAccount);
                } else {
                    Console.error("Invalid account number!");
                }
            } catch (NumberFormatException e) {
                Console.error("Invalid input!");
            }
        }
    }

    private void showTransactionMenu(Account account) {
        while (true) {
            Console.line();
            Console.info("Admin Transaction Management for Account: " + account.getAccountType());
            Console.info("Balance: $" + String.format("%.2f", account.getBalance()));
            Console.info("1) View transaction history");
            Console.info("2) View filtered transactions");
            Console.info("3) View all customer transactions");
            Console.info("4) Add deposit");
            Console.info("5) Add withdrawal");
            Console.info("6) Add transfer");
            Console.info("7) Delete transaction");
            Console.info("0) Back");
            Console.line();

            String choice = Console.ask("Enter choice: ");
            switch (choice) {
                case "0":
                    return;
                case "1":
                    transactionController.viewTransactionHistory(account);
                    break;
                case "2":
                    transactionController.viewFilteredTransactionHistory(account);
                    break;
                case "3":
                    transactionController.viewAllCustomerTransactions(account.getCustomer());
                    break;
                case "4":
                    transactionController.addDeposit(account);
                    break;
                case "5":
                    transactionController.addWithdrawal(account);
                    break;
                case "6":
                    transactionController.addTransfer(account);
                    break;
                case "7":
                    transactionController.deleteTransaction(account);
                    break;
                default:
                    Console.error("Invalid option!");
                    break;
            }
        }
    }

    // Add a new menu option for viewing all transactions in the system (admin only)
    public void viewAllSystemTransactions() {
        try {
            ArrayList<Transaction> allTransactions = transactionRepository.all();

            if (allTransactions.isEmpty()) {
                Console.warning("No transactions found in the system.");
                return;
            }

            Console.line();
            Console.info("All System Transactions");
            Console.info("Total Transactions: " + allTransactions.size());
            Console.line();

            // Enhanced menu for system transactions
            while (true) {
                Console.info("System Transaction Options:");
                Console.info("1) View all transactions");
                Console.info("2) View filtered transactions");
                Console.info("3) View transactions by customer");
                Console.info("4) View transaction statistics");
                Console.info("0) Back");
                Console.line();

                String choice = Console.ask("Enter choice: ");
                switch (choice) {
                    case "0":
                        return;
                    case "1":
                        displaySystemTransactions(allTransactions);
                        break;
                    case "2":
                        filterAndDisplaySystemTransactions(allTransactions);
                        break;
                    case "3":
                        viewTransactionsByCustomer();
                        break;
                    case "4":
                        displaySystemTransactionStatistics(allTransactions);
                        break;
                    default:
                        Console.error("Invalid option!");
                        break;
                }
                Console.line();
            }

        } catch (Exception e) {
            Console.error("View failed: An unexpected error occurred. " + e.getMessage());
        }
    }

    private void filterAndDisplaySystemTransactions(ArrayList<Transaction> allTransactions) {
        ArrayList<Transaction> filteredTransactions = filterService.filterTransactions(allTransactions);
        if (filteredTransactions.isEmpty()) {
            Console.warning("No transactions match the selected filters.");
            return;
        }
        displaySystemTransactions(filteredTransactions);
    }

    private void viewTransactionsByCustomer() {
        List<Customer> customers = customerRepository.all();
        if (customers.isEmpty()) {
            Console.warning("No customers found.");
            return;
        }

        Console.line();
        Console.info("Select Customer:");
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            Console.info((i + 1) + ") " + c.getFullName() + " (" + c.getEmail() + ") - " + c.getUserType());
        }
        Console.line();

        String choice = Console.ask("Enter customer number (0 to cancel): ");
        if (!choice.equals("0")) {
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < customers.size()) {
                    Customer selectedCustomer = customers.get(index);
                    viewCustomerTransactionsAdmin(selectedCustomer);
                } else {
                    Console.error("Invalid customer number!");
                }
            } catch (NumberFormatException e) {
                Console.error("Invalid input!");
            }
        }
    }

    private void viewCustomerTransactionsAdmin(Customer customer) {
        try {
            if (customer.getUserType() == UserType.ADMIN) {
                Console.warning("Admin users do not have transaction accounts.");
                return;
            }

            List<Account> accounts = customer.getAccounts();
            if (accounts.isEmpty()) {
                Console.warning("No accounts found for this customer.");
                return;
            }

            // Collect all transactions from customer's accounts
            ArrayList<Transaction> customerTransactions = new ArrayList<>();
            for (Account account : accounts) {
                customerTransactions.addAll(account.getTransactions());
            }

            if (customerTransactions.isEmpty()) {
                Console.warning("No transactions found for this customer.");
                return;
            }

            Console.line();
            Console.info("Transaction Management for Customer: " + customer.getFullName());
            Console.info("Total Accounts: " + accounts.size());
            Console.info("Total Transactions: " + customerTransactions.size());
            Console.line();

            while (true) {
                Console.info("Customer Transaction Options:");
                Console.info("1) View all customer transactions");
                Console.info("2) View filtered customer transactions");
                Console.info("3) View transactions by account");
                Console.info("4) View customer transaction statistics");
                Console.info("0) Back");
                Console.line();

                String choice = Console.ask("Enter choice: ");
                switch (choice) {
                    case "0":
                        return;
                    case "1":
                        filterService.displayFilteredTransactions(customerTransactions, customer);
                        break;
                    case "2":
                        filterAndDisplayCustomerTransactions(customerTransactions, customer);
                        break;
                    case "3":
                        viewTransactionsByAccount(customer);
                        break;
                    case "4":
                        statisticsService.displayCustomerTransactionStatistics(customerTransactions, customer);
                        break;
                    default:
                        Console.error("Invalid option!");
                        break;
                }
                Console.line();
            }

        } catch (Exception e) {
            Console.error("View failed: An unexpected error occurred. " + e.getMessage());
        }
    }

    private void filterAndDisplayCustomerTransactions(ArrayList<Transaction> customerTransactions, Customer customer) {
        ArrayList<Transaction> filteredTransactions = filterService.filterTransactions(customerTransactions);
        if (filteredTransactions.isEmpty()) {
            Console.warning("No transactions match the selected filters.");
            return;
        }
        filterService.displayFilteredTransactions(filteredTransactions, customer);
    }

    private void viewTransactionsByAccount(Customer customer) {
        List<Account> accounts = customer.getAccounts();

        Console.line();
        Console.info("Select Account:");
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            Console.info((i + 1) + ") " + account.getAccountType() +
                    " - Balance: $" + String.format("%.2f", account.getBalance()) +
                    " - Transactions: " + account.getTransactions().size());
        }
        Console.line();

        String choice = Console.ask("Enter account number (0 to cancel): ");
        if (!choice.equals("0")) {
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < accounts.size()) {
                    Account selectedAccount = accounts.get(index);

                    Console.line();
                    Console.info("Account Transaction Options:");
                    Console.info("1) View all account transactions");
                    Console.info("2) View filtered account transactions");
                    Console.info("0) Back");

                    String subChoice = Console.ask("Enter choice: ");
                    switch (subChoice) {
                        case "1":
                            transactionController.viewTransactionHistory(selectedAccount);
                            break;
                        case "2":
                            transactionController.viewFilteredTransactionHistory(selectedAccount);
                            break;
                    }
                } else {
                    Console.error("Invalid account number!");
                }
            } catch (NumberFormatException e) {
                Console.error("Invalid input!");
            }
        }
    }

    private void displaySystemTransactionStatistics(ArrayList<Transaction> transactions) {
        statisticsService.displaySystemStatistics(transactions);
    }

    private void displaySystemTransactions(ArrayList<Transaction> transactions) {
        Console.line();
        Console.success("System Transactions (" + transactions.size() + " total):");
        Console.line();

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);

            // Get customer names
            String sourceCustomer = t.getSourceAccount().getCustomer().getFullName();
            String destCustomer = t.getDestinationAccount().getCustomer().getFullName();

            String transactionInfo = "";
            if (t.getTransactionType() == TransactionType.TRANSFER &&
                    !sourceCustomer.equals(destCustomer)) {
                transactionInfo = sourceCustomer + " -> " + destCustomer;
            } else {
                transactionInfo = sourceCustomer;
            }

            Console.info((i + 1) + ") " + t.getTransactionType() + " | $" +
                    String.format("%.2f", t.getAmount()) + " | " + transactionInfo +
                    " | " + t.getDescription() + " | " + t.getDate() + " | ID: " + t.getId());
        }
        Console.line();
    }

}