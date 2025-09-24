package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Account;
import models.AccountType;
import models.Customer;
import models.Transaction;
import models.TransactionType;
import models.UserType;
import repositories.AccountRepository;
import repositories.CustomerRepository;
import services.AuthInterface;
import services.FilterService;
import services.StatisticsService;
import utils.Console;

public final class CustomerController {
    private AuthInterface auth;
    private CustomerRepository customerRepository;
    private AccountRepository accountRepository;
    private TransactionController transactionController;
    private FilterService filterService;
    private StatisticsService statisticsService;

    public CustomerController(AuthInterface auth) {
        this.auth = auth;
        this.customerRepository = CustomerRepository.getInstance();
        this.accountRepository = AccountRepository.getInstance();
        this.transactionController = new TransactionController();
        this.filterService = FilterService.getInstance();
        this.statisticsService = StatisticsService.getInstance();
    }

    public void createCustomer() {
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

    public void profile(Customer c, String label) {
        Console.line();
        Console.success("=== " + label + " ===");
        Console.info("ID: " + c.getId());
        Console.info("Name: " + c.getFullName());
        Console.info("Email: " + c.getEmail());
        Console.info("User Type: " + c.getUserType());
        Console.info("Number of Accounts: " + c.getAccounts().size());
        Console.line();
    }

    public void manageMyAccounts() {
        Customer currentUser = auth.requireAuthentication();

        while (true) {
            Console.line();
            Console.info("My Banking Dashboard - " + currentUser.getFullName());
            Console.info("1) View my personal information");
            Console.info("2) View my accounts summary");
            Console.info("3) View detailed account information");
            Console.info("4) View all my transactions");
            Console.info("5) Manage specific account transactions");
            Console.info("6) View my banking statistics");
            Console.info("7) Create new account");
            Console.info("0) Back");
            Console.line();

            String choice = Console.ask("Enter choice: ");
            switch (choice) {
                case "0":
                    return;
                case "1":
                    viewMyPersonalInformation(currentUser);
                    break;
                case "2":
                    viewMyAccountsSummary(currentUser);
                    break;
                case "3":
                    viewDetailedAccountInformation(currentUser);
                    break;
                case "4":
                    viewAllMyTransactions(currentUser);
                    break;
                case "5":
                    manageAccountTransactions(currentUser);
                    break;
                case "6":
                    viewMyBankingStatistics(currentUser);
                    break;
                case "7":
                    createAccount(currentUser);
                    break;
                default:
                    Console.error("Invalid option!");
                    break;
            }
        }
    }

    private void viewMyPersonalInformation(Customer customer) {
        Console.line();
        Console.success("=== My Personal Information ===");
        Console.info("Customer ID: " + customer.getId());
        Console.info("Full Name: " + customer.getFullName());
        Console.info("First Name: " + customer.getFirstName());
        Console.info("Last Name: " + customer.getLastName());
        Console.info("Email: " + customer.getEmail());
        Console.info("Account Type: " + customer.getUserType());
        Console.info("Number of Bank Accounts: " + customer.getAccounts().size());

        // Calculate total balance across all accounts
        double totalBalance = customer.getAccounts().stream()
                .mapToDouble(Account::getBalance)
                .sum();
        Console.info("Total Balance Across All Accounts: $" + String.format("%.2f", totalBalance));

        // Show account types owned
        if (!customer.getAccounts().isEmpty()) {
            Console.line();
            Console.info("Account Types You Own:");
            customer.getAccounts().forEach(
                    account -> Console.info("  • " + account.getAccountType() + " (ID: " + account.getId() + ")"));
        }
        Console.line();
    }

    private void viewMyAccountsSummary(Customer customer) {
        List<Account> accounts = customer.getAccounts();
        if (accounts.isEmpty()) {
            Console.warning("You have no bank accounts yet.");
            Console.info("Would you like to create an account? Use option 7 from the main menu.");
            return;
        }

        Console.line();
        Console.success("=== My Accounts Summary ===");

        double grandTotal = 0;
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            double balance = account.getBalance();
            grandTotal += balance;

            Console.info((i + 1) + ") " + account.getAccountType());
            Console.info("   Account ID: " + account.getId());
            Console.info("   Current Balance: $" + String.format("%.2f", balance));
            Console.info("   Number of Transactions: " + account.getTransactions().size());

            // Show recent transaction if available
            if (!account.getTransactions().isEmpty()) {
                Transaction lastTransaction = account.getTransactions().get(account.getTransactions().size() - 1);
                Console.info("   Last Transaction: " + lastTransaction.getTransactionType() +
                        " - $" + String.format("%.2f", lastTransaction.getAmount()) +
                        " on " + lastTransaction.getDate());
            }
            Console.line();
        }

        Console.success("Total Balance Across All Accounts: $" + String.format("%.2f", grandTotal));
        Console.line();
    }

    private void viewDetailedAccountInformation(Customer customer) {
        List<Account> accounts = customer.getAccounts();
        if (accounts.isEmpty()) {
            Console.warning("You have no accounts yet.");
            return;
        }

        Console.line();
        Console.info("Select account for detailed information:");
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            Console.info((i + 1) + ") " + account.getAccountType() +
                    " - Balance: $" + String.format("%.2f", account.getBalance()));
        }
        Console.line();

        String choice = Console.ask("Enter account number (0 to cancel): ");
        if (!choice.equals("0")) {
            try {
                int index = Integer.parseInt(choice) - 1;
                if (index >= 0 && index < accounts.size()) {
                    Account selectedAccount = accounts.get(index);
                    showDetailedAccountInfo(selectedAccount);
                } else {
                    Console.error("Invalid account number!");
                }
            } catch (NumberFormatException e) {
                Console.error("Invalid input!");
            }
        }
    }

    private void showDetailedAccountInfo(Account account) {
        Console.line();
        Console.success("=== Detailed Account Information ===");
        Console.info("Account Type: " + account.getAccountType());
        Console.info("Account ID: " + account.getId());
        Console.info("Current Balance: $" + String.format("%.2f", account.getBalance()));
        Console.info("Owner: " + account.getCustomer().getFullName());

        List<Transaction> transactions = account.getTransactions();
        Console.info("Total Transactions: " + transactions.size());

        if (!transactions.isEmpty()) {
            // Calculate statistics for this account
            double totalDeposits = transactions.stream()
                    .filter(t -> t.getTransactionType() == TransactionType.DEPOSIT ||
                            (t.getTransactionType() == TransactionType.TRANSFER &&
                                    t.getDestinationAccount().equals(account)))
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            double totalWithdrawals = transactions.stream()
                    .filter(t -> t.getTransactionType() == TransactionType.WITHDRAWAL ||
                            (t.getTransactionType() == TransactionType.TRANSFER &&
                                    t.getSourceAccount().equals(account)))
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            Console.line();
            Console.info("Account Transaction Statistics:");
            Console.info("  Total Money In: +$" + String.format("%.2f", totalDeposits));
            Console.info("  Total Money Out: -$" + String.format("%.2f", totalWithdrawals));
            Console.info("  Net Change: $" + String.format("%.2f", (totalDeposits - totalWithdrawals)));

            // Show first and last transaction dates
            Console.info("  First Transaction: " + transactions.get(0).getDate());
            Console.info("  Last Transaction: " + transactions.get(transactions.size() - 1).getDate());
        }
        Console.line();
    }

    private void viewAllMyTransactions(Customer customer) {
        List<Account> accounts = customer.getAccounts();
        if (accounts.isEmpty()) {
            Console.warning("You have no accounts with transactions.");
            return;
        }

        Console.line();
        Console.info("All My Transactions Options:");
        Console.info("1) View all transactions (all accounts)");
        Console.info("2) View filtered transactions");
        Console.info("3) View sorted transactions");
        Console.info("0) Back");
        Console.line();

        String choice = Console.ask("Enter choice: ");
        switch (choice) {
            case "0":
                return;
            case "1":
                transactionController.viewAllCustomerTransactions(customer);
                break;
            case "2":
                viewFilteredTransactions(customer);
                break;
            case "3":
                viewSortedTransactions(customer);
                break;
            default:
                Console.error("Invalid option!");
                break;
        }
    }

    private void viewFilteredTransactions(Customer customer) {
        // Collect all transactions from all accounts
        ArrayList<Transaction> allTransactions = new ArrayList<>();
        for (Account account : customer.getAccounts()) {
            allTransactions.addAll(account.getTransactions());
        }

        if (allTransactions.isEmpty()) {
            Console.warning("You have no transactions to filter.");
            return;
        }

        Console.line();
        Console.info("Filter Your Transactions:");
        Console.info("Total transactions available: " + allTransactions.size());

        ArrayList<Transaction> filteredTransactions = filterService.filterTransactions(allTransactions);

        if (filteredTransactions.isEmpty()) {
            Console.warning("No transactions match your filter criteria.");
            return;
        }

        filterService.displayFilteredTransactions(filteredTransactions, customer);
    }

    private void viewSortedTransactions(Customer customer) {
        // Collect all transactions from all accounts
        ArrayList<Transaction> allTransactions = new ArrayList<>();
        for (Account account : customer.getAccounts()) {
            allTransactions.addAll(account.getTransactions());
        }

        if (allTransactions.isEmpty()) {
            Console.warning("You have no transactions to sort.");
            return;
        }

        ArrayList<Transaction> sortedTransactions = filterService.sortTransactions(allTransactions);
        filterService.displayFilteredTransactions(sortedTransactions, customer);
    }

    private void viewMyBankingStatistics(Customer customer) {
        statisticsService.displayCustomerStatistics(customer);
    }

    private void manageAccountTransactions(Customer customer) {
        List<Account> accounts = customer.getAccounts();
        if (accounts.isEmpty()) {
            Console.warning("You have no accounts yet.");
            return;
        }

        Console.line();
        Console.info("Select account to manage transactions:");
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
                    showUserTransactionMenu(selectedAccount);
                } else {
                    Console.error("Invalid account number!");
                }
            } catch (NumberFormatException e) {
                Console.error("Invalid input!");
            }
        }
    }

    private void createAccount(Customer customer) {
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
                    Console.error("You already have a " + selectedType + " account.");
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

    private void showUserTransactionMenu(Account account) {
        while (true) {
            Console.line();
            Console.info("Transaction Management for Account: " + account.getAccountType());
            Console.info("Balance: $" + String.format("%.2f", account.getBalance()));
            Console.info("1) View transaction history");
            Console.info("2) View filtered transactions");
            Console.info("3) View all my transactions (all accounts)");
            Console.info("4) Make deposit");
            Console.info("5) Make withdrawal");
            Console.info("6) Make transfer");
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
                default:
                    Console.error("Invalid option!");
                    break;
            }
        }
    }

}
