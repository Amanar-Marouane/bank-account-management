package controllers;

import models.Account;
import models.Customer;
import models.Transaction;
import models.TransactionType;
import repositories.AccountRepository;
import repositories.CustomerRepository;
import repositories.TransactionRepository;
import utils.Console;
import exceptions.NegativeAmountException;
import exceptions.CustomerNotFoundException;
import exceptions.AccountNotFoundException;
import exceptions.InvalidTransactionException;
import exceptions.InsufficientFundsException;
import exceptions.SuspiciousActivityException;
import services.FilterService;
import services.StatisticsService;
import services.SuspiciousTransactionDetector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TransactionController {
    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;
    private CustomerRepository customerRepository;
    private FilterService filterService;
    private StatisticsService statisticsService;
    private SuspiciousTransactionDetector suspiciousDetector;

    public TransactionController() {
        this.transactionRepository = TransactionRepository.getInstance();
        this.accountRepository = AccountRepository.getInstance();
        this.customerRepository = CustomerRepository.getInstance();
        this.filterService = FilterService.getInstance();
        this.statisticsService = StatisticsService.getInstance();
        this.suspiciousDetector = new SuspiciousTransactionDetector();
    }

    public void viewTransactionHistory(Account account) {
        List<Transaction> transactions = account.getTransactions();
        if (transactions.isEmpty()) {
            Console.warning("No transactions found for this account.");
            return;
        }

        Console.line();
        Console.info("Transaction History for Account: " + account.getAccountType());
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            String sign = "";
            if (t.getTransactionType() == TransactionType.DEPOSIT ||
                    (t.getTransactionType() == TransactionType.TRANSFER && t.getDestinationAccount().equals(account))) {
                sign = "+";
            } else {
                sign = "-";
            }
            Console.info((i + 1) + ") " + t.getTransactionType() + " | " + sign + "$" +
                    String.format("%.2f", t.getAmount()) + " | " + t.getDescription() +
                    " | " + t.getFormattedDateTime() + " | ID: " + t.getId());
        }
        Console.line();
    }

    public void addDeposit(Account account) {
        try {
            String amountStr = Console.ask("Enter deposit amount: $");
            double amount = parseAmount(amountStr);

            validatePositiveAmount(amount);
            validateAccount(account);

            String description = Console.ask("Enter description: ");
            if (description.trim().isEmpty()) {
                description = "Deposit";
            }

            Transaction deposit = new Transaction(
                    TransactionType.DEPOSIT,
                    amount,
                    description,
                    account,
                    account);

            // Check for suspicious activity
            validateSuspiciousActivity(account.getTransactions(), deposit);

            transactionRepository.save(deposit);
            Console.success("Deposit added successfully!");
            Console.info("New balance: $" + String.format("%.2f", account.getBalance()));

        } catch (SuspiciousActivityException e) {
            Console.error("Deposit blocked: " + e.getMessage());
        } catch (NegativeAmountException e) {
            Console.error("Deposit failed: " + e.getMessage());
        } catch (AccountNotFoundException e) {
            Console.error("Deposit failed: " + e.getMessage());
        } catch (InvalidTransactionException e) {
            Console.error("Deposit failed: " + e.getMessage());
        } catch (NumberFormatException e) {
            Console.error("Deposit failed: Invalid amount format. Please enter a valid number.");
        } catch (Exception e) {
            Console.error("Deposit failed: An unexpected error occurred. Please try again.");
        }
    }

    public void addWithdrawal(Account account) {
        try {
            String amountStr = Console.ask("Enter withdrawal amount: $");
            double amount = parseAmount(amountStr);

            validatePositiveAmount(amount);
            validateAccount(account);
            validateSufficientFunds(account, amount);

            String description = Console.ask("Enter description: ");
            if (description.trim().isEmpty()) {
                description = "Withdrawal";
            }

            Transaction withdrawal = new Transaction(
                    TransactionType.WITHDRAWAL,
                    amount,
                    description,
                    account,
                    account);

            // Check for suspicious activity
            validateSuspiciousActivity(account.getTransactions(), withdrawal);

            transactionRepository.save(withdrawal);
            Console.success("Withdrawal added successfully!");
            Console.info("New balance: $" + String.format("%.2f", account.getBalance()));

        } catch (SuspiciousActivityException e) {
            Console.error("Withdrawal blocked: " + e.getMessage());
        } catch (NegativeAmountException e) {
            Console.error("Withdrawal failed: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            Console.error("Withdrawal failed: " + e.getMessage());
        } catch (AccountNotFoundException e) {
            Console.error("Withdrawal failed: " + e.getMessage());
        } catch (InvalidTransactionException e) {
            Console.error("Withdrawal failed: " + e.getMessage());
        } catch (NumberFormatException e) {
            Console.error("Withdrawal failed: Invalid amount format. Please enter a valid number.");
        } catch (Exception e) {
            Console.error("Withdrawal failed: An unexpected error occurred. Please try again.");
        }
    }

    public void addTransfer(Account sourceAccount) {
        try {
            String amountStr = Console.ask("Enter transfer amount: $");
            double amount = parseAmount(amountStr);

            validatePositiveAmount(amount);
            validateAccount(sourceAccount);
            validateSufficientFunds(sourceAccount, amount);

            Account destinationAccount = selectDestinationAccount(sourceAccount);
            if (destinationAccount == null) {
                return; // User cancelled
            }

            validateAccount(destinationAccount);
            validateTransferAccounts(sourceAccount, destinationAccount);

            String description = Console.ask("Enter description: ");
            if (description.trim().isEmpty()) {
                description = "Transfer";
            }

            Transaction transfer = new Transaction(
                    TransactionType.TRANSFER,
                    amount,
                    description,
                    sourceAccount,
                    destinationAccount);

            // Check for suspicious activity
            validateSuspiciousActivity(sourceAccount.getTransactions(), transfer);

            transactionRepository.save(transfer);
            Console.success("Transfer completed successfully!");
            Console.info("Source account new balance: $" + String.format("%.2f", sourceAccount.getBalance()));
            Console.info("Destination account new balance: $" + String.format("%.2f", destinationAccount.getBalance()));

        } catch (SuspiciousActivityException e) {
            Console.error("Transfer blocked: " + e.getMessage());
        } catch (NegativeAmountException e) {
            Console.error("Transfer failed: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            Console.error("Transfer failed: " + e.getMessage());
        } catch (AccountNotFoundException e) {
            Console.error("Transfer failed: " + e.getMessage());
        } catch (InvalidTransactionException e) {
            Console.error("Transfer failed: " + e.getMessage());
        } catch (CustomerNotFoundException e) {
            Console.error("Transfer failed: " + e.getMessage());
        } catch (NumberFormatException e) {
            Console.error("Transfer failed: Invalid amount format. Please enter a valid number.");
        } catch (Exception e) {
            Console.error("Transfer failed: An unexpected error occurred. Please try again.");
        }
    }

    public void deleteTransaction(Account account) {
        try {
            validateAccount(account);

            List<Transaction> transactions = account.getTransactions();
            if (transactions.isEmpty()) {
                Console.warning("No transactions found for this account.");
                return;
            }

            displayTransactionsForDeletion(transactions);

            String choice = Console.ask("Enter transaction number to delete (0 to cancel): ");
            if (!choice.equals("0")) {
                int index = Integer.parseInt(choice) - 1;
                validateTransactionIndex(index, transactions.size());

                Transaction transactionToDelete = transactions.get(index);
                validateTransactionDeletion(transactionToDelete);

                if (Console.confirm(
                        "Are you sure you want to delete this transaction? This will affect account balances.")) {
                    performTransactionDeletion(transactionToDelete, account);
                    Console.success("Transaction deleted successfully!");
                    Console.info("Updated balance: $" + String.format("%.2f", account.getBalance()));
                }
            }
        } catch (AccountNotFoundException e) {
            Console.error("Delete failed: " + e.getMessage());
        } catch (InvalidTransactionException e) {
            Console.error("Delete failed: " + e.getMessage());
        } catch (NumberFormatException e) {
            Console.error("Delete failed: Invalid transaction number. Please enter a valid number.");
        } catch (IndexOutOfBoundsException e) {
            Console.error("Delete failed: Invalid transaction selection.");
        } catch (Exception e) {
            Console.error("Delete failed: An unexpected error occurred. Please try again.");
        }
    }

    public void viewFilteredTransactionHistory(Account account) {
        try {
            List<Transaction> transactions = account.getTransactions();
            if (transactions.isEmpty()) {
                Console.warning("No transactions found for this account.");
                return;
            }

            Console.line();
            Console.info("Transaction Filtering for Account: " + account.getAccountType());
            Console.info("Current Balance: $" + String.format("%.2f", account.getBalance()));

            ArrayList<Transaction> filteredTransactions = filter(new ArrayList<>(transactions));

            if (filteredTransactions.isEmpty()) {
                Console.warning("No transactions match the selected filters.");
                return;
            }

            displayFilteredResults(filteredTransactions, account);

        } catch (Exception e) {
            Console.error("Filter failed: An unexpected error occurred. " + e.getMessage());
        }
    }

    public void viewAllCustomerTransactions(Customer customer) {
        try {
            List<Account> accounts = customer.getAccounts();
            if (accounts.isEmpty()) {
                Console.warning("No accounts found for this customer.");
                return;
            }

            // Collect all transactions from all accounts
            ArrayList<Transaction> allTransactions = new ArrayList<>();
            for (Account account : accounts) {
                allTransactions.addAll(account.getTransactions());
            }

            if (allTransactions.isEmpty()) {
                Console.warning("No transactions found for any account.");
                return;
            }

            Console.line();
            Console.info("All Transactions for Customer: " + customer.getFullName());
            Console.info("Total Accounts: " + accounts.size());
            Console.info("Total Transactions: " + allTransactions.size());

            // Ask if user wants to filter
            String filterChoice = Console.ask("Do you want to filter transactions? (y/N): ");
            if (filterChoice.toLowerCase().equals("y") || filterChoice.toLowerCase().equals("yes")) {
                ArrayList<Transaction> filteredTransactions = filter(allTransactions);
                if (filteredTransactions.isEmpty()) {
                    Console.warning("No transactions match the selected filters.");
                    return;
                }
                filterService.displayFilteredTransactions(filteredTransactions, customer);
            } else {
                filterService.displayFilteredTransactions(allTransactions, customer);
            }

        } catch (Exception e) {
            Console.error("View failed: An unexpected error occurred. " + e.getMessage());
        }
    }

    // Helper methods for validation
    private double parseAmount(String amountStr) throws NumberFormatException {
        return Double.parseDouble(amountStr);
    }

    private void validatePositiveAmount(double amount) throws NegativeAmountException {
        if (amount <= 0) {
            throw new NegativeAmountException(amount);
        }
    }

    private void validateAccount(Account account) throws AccountNotFoundException {
        if (account == null) {
            throw new AccountNotFoundException("Account is null");
        }
    }

    private void validateSufficientFunds(Account account, double amount) throws InsufficientFundsException {
        if (amount > account.getBalance()) {
            throw new InsufficientFundsException(amount, account.getBalance());
        }
    }

    private void validateTransferAccounts(Account source, Account destination) throws InvalidTransactionException {
        if (source.getId().equals(destination.getId())) {
            throw new InvalidTransactionException("transfer", "Cannot transfer to the same account");
        }
    }

    private void validateTransactionIndex(int index, int size) throws InvalidTransactionException {
        if (index < 0 || index >= size) {
            throw new InvalidTransactionException("deletion", "Invalid transaction selection");
        }
    }

    private void validateTransactionDeletion(Transaction transaction) throws InvalidTransactionException {
        if (transaction == null) {
            throw new InvalidTransactionException("deletion", "Transaction not found");
        }
    }

    private Account selectDestinationAccount(Account sourceAccount)
            throws AccountNotFoundException, CustomerNotFoundException {
        List<Account> allAccounts = accountRepository.all();
        List<Account> availableAccounts = allAccounts.stream()
                .filter(acc -> !acc.getId().equals(sourceAccount.getId()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        if (availableAccounts.isEmpty()) {
            throw new AccountNotFoundException("No other accounts available for transfer");
        }

        Console.line();
        Console.info("Select destination account:");
        for (int i = 0; i < availableAccounts.size(); i++) {
            Account acc = availableAccounts.get(i);
            Optional<Customer> owner = customerRepository.findCustomerByAccount(acc);
            String ownerName = owner.isPresent() ? owner.get().getFullName() : "Unknown";
            Console.info((i + 1) + ") " + acc.getAccountType() + " | " + ownerName +
                    " | Balance: $" + String.format("%.2f", acc.getBalance()) +
                    " | ID: " + acc.getId());
        }
        Console.line();

        String choice = Console.ask("Enter destination account number (0 to cancel): ");
        if (choice.equals("0")) {
            return null;
        }

        try {
            int index = Integer.parseInt(choice) - 1;
            if (index < 0 || index >= availableAccounts.size()) {
                throw new AccountNotFoundException("Invalid account selection");
            }
            return availableAccounts.get(index);
        } catch (NumberFormatException e) {
            throw new InvalidTransactionException("transfer", "Invalid account selection format");
        }
    }

    private void displayTransactionsForDeletion(List<Transaction> transactions) {
        Console.line();
        Console.info("Select transaction to delete:");
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            String sign = "";
            if (t.getTransactionType() == TransactionType.DEPOSIT ||
                    (t.getTransactionType() == TransactionType.TRANSFER
                            && t.getDestinationAccount().equals(t.getSourceAccount()))) {
                sign = "+";
            } else {
                sign = "-";
            }
            Console.info((i + 1) + ") " + t.getTransactionType() + " | " + sign + "$" +
                    String.format("%.2f", t.getAmount()) + " | " + t.getDescription() +
                    " | " + t.getFormattedDateTime());
        }
        Console.line();
    }

    private void performTransactionDeletion(Transaction transactionToDelete, Account account)
            throws InvalidTransactionException {
        try {
            // Create reverse transaction to undo the balance effects
            Transaction reverseTransaction = createReverseTransaction(transactionToDelete);

            // Apply the reverse transaction to fix balances
            transactionToDelete.getSourceAccount().addTransaction(reverseTransaction);
            if (!transactionToDelete.getSourceAccount().equals(transactionToDelete.getDestinationAccount())) {
                transactionToDelete.getDestinationAccount().addTransaction(reverseTransaction);
            }

            // Remove from repositories and accounts
            transactionRepository.delete(transactionToDelete);
            account.getTransactions().remove(transactionToDelete);

            // If it's a transfer, also remove from destination account
            if (transactionToDelete.getTransactionType() == TransactionType.TRANSFER &&
                    !transactionToDelete.getDestinationAccount().equals(account)) {
                transactionToDelete.getDestinationAccount().getTransactions().remove(transactionToDelete);
            }
        } catch (Exception e) {
            throw new InvalidTransactionException("deletion",
                    "Failed to reverse transaction effects: " + e.getMessage());
        }
    }

    private Transaction createReverseTransaction(Transaction originalTransaction) {
        Account sourceAccount = originalTransaction.getSourceAccount();
        Account destinationAccount = originalTransaction.getDestinationAccount();
        double amount = originalTransaction.getAmount();

        switch (originalTransaction.getTransactionType()) {
            case DEPOSIT:
                return new Transaction(
                        TransactionType.WITHDRAWAL,
                        amount,
                        "Reversal of deleted deposit",
                        sourceAccount,
                        sourceAccount);
            case WITHDRAWAL:
                return new Transaction(
                        TransactionType.DEPOSIT,
                        amount,
                        "Reversal of deleted withdrawal",
                        sourceAccount,
                        sourceAccount);
            case TRANSFER:
                return new Transaction(
                        TransactionType.TRANSFER,
                        amount,
                        "Reversal of deleted transfer",
                        destinationAccount,
                        sourceAccount);
            default:
                throw new InvalidTransactionException("reversal",
                        "Unknown transaction type: " + originalTransaction.getTransactionType());
        }
    }

    private void displayFilteredResults(ArrayList<Transaction> transactions, Account account) {
        Console.line();
        Console.success("Filtered Results (" + transactions.size() + " transactions):");
        Console.line();

        double totalIn = 0, totalOut = 0;

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            String sign = "";
            double effectiveAmount = 0;

            if (t.getTransactionType() == TransactionType.DEPOSIT ||
                    (t.getTransactionType() == TransactionType.TRANSFER && t.getDestinationAccount().equals(account))) {
                sign = "+";
                effectiveAmount = t.getAmount();
                totalIn += effectiveAmount;
            } else {
                sign = "-";
                effectiveAmount = t.getAmount();
                totalOut += effectiveAmount;
            }

            Console.info((i + 1) + ") " + t.getTransactionType() + " | " + sign + "$" +
                    String.format("%.2f", effectiveAmount) + " | " + t.getDescription() +
                    " | " + t.getFormattedDateTime() + " | ID: " + t.getId());
        }

        Console.line();
        Console.success("Summary:");
        Console.info("Total Credits: +$" + String.format("%.2f", totalIn));
        Console.info("Total Debits: -$" + String.format("%.2f", totalOut));
        Console.info("Net Change: $" + String.format("%.2f", (totalIn - totalOut)));
        Console.line();
    }

    public ArrayList<Transaction> filter(ArrayList<Transaction> transactions) {
        return filterService.filterTransactions(transactions);
    }

    public ArrayList<Transaction> sort(ArrayList<Transaction> transactions) {
        return filterService.sortTransactions(transactions);
    }

    public void displayStatistics(Customer customer) {
        statisticsService.displayCustomerStatistics(customer);
    }

    public void displayAccountStatistics(Account account) {
        statisticsService.displayAccountStatistics(account);
    }

    public void displaySystemStatistics(ArrayList<Transaction> transactions) {
        statisticsService.displaySystemStatistics(transactions);
    }

    private void validateSuspiciousActivity(List<Transaction> transactions, Transaction newTransaction)
            throws SuspiciousActivityException {
        suspiciousDetector.validateTransaction(transactions, newTransaction);
    }

}
