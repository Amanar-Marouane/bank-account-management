package services;

import models.Account;
import models.Customer;
import models.Transaction;
import models.TransactionType;
import utils.Console;

import java.util.ArrayList;
import java.util.List;

public class StatisticsService {
    private static StatisticsService instance;

    private StatisticsService() {
    }

    public static StatisticsService getInstance() {
        if (instance == null) {
            instance = new StatisticsService();
        }
        return instance;
    }

    public void displayCustomerStatistics(Customer customer) {
        List<Account> accounts = customer.getAccounts();

        Console.line();
        Console.success("=== Statistics for " + customer.getFullName() + " ===");

        if (accounts.isEmpty()) {
            Console.warning("No accounts found. Create an account to see statistics.");
            return;
        }

        // Collect all transactions
        ArrayList<Transaction> allTransactions = new ArrayList<>();
        for (Account account : accounts) {
            allTransactions.addAll(account.getTransactions());
        }

        displayAccountOverview(accounts);
        displayTransactionOverview(allTransactions);
        displayFinancialFlowAnalysis(allTransactions, customer);
        displayActivityAnalysis(allTransactions);
        Console.line();
    }

    public void displayAccountStatistics(Account account) {
        List<Transaction> transactions = account.getTransactions();

        Console.line();
        Console.success("=== Account Statistics ===");
        Console.info("Account Type: " + account.getAccountType());
        Console.info("Account ID: " + account.getId());
        Console.info("Current Balance: $" + String.format("%.2f", account.getBalance()));
        Console.info("Owner: " + account.getCustomer().getFullName());
        Console.info("Total Transactions: " + transactions.size());

        if (!transactions.isEmpty()) {
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

            Console.info("  First Transaction: " + transactions.get(0).getDate());
            Console.info("  Last Transaction: " + transactions.get(transactions.size() - 1).getDate());
        }
        Console.line();
    }

    public void displaySystemStatistics(ArrayList<Transaction> transactions) {
        Console.line();
        Console.success("=== System Transaction Statistics ===");
        Console.line();

        // Count by type
        long deposits = transactions.stream().filter(t -> t.getTransactionType() == TransactionType.DEPOSIT).count();
        long withdrawals = transactions.stream().filter(t -> t.getTransactionType() == TransactionType.WITHDRAWAL)
                .count();
        long transfers = transactions.stream().filter(t -> t.getTransactionType() == TransactionType.TRANSFER).count();

        // Calculate totals
        double totalDeposits = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.DEPOSIT)
                .mapToDouble(Transaction::getAmount).sum();
        double totalWithdrawals = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.WITHDRAWAL)
                .mapToDouble(Transaction::getAmount).sum();
        double totalTransfers = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.TRANSFER)
                .mapToDouble(Transaction::getAmount).sum();

        Console.info("Transaction Count by Type:");
        Console.info("  Deposits: " + deposits + " transactions");
        Console.info("  Withdrawals: " + withdrawals + " transactions");
        Console.info("  Transfers: " + transfers + " transactions");
        Console.line();

        Console.info("Transaction Amount by Type:");
        Console.info("  Total Deposits: $" + String.format("%.2f", totalDeposits));
        Console.info("  Total Withdrawals: $" + String.format("%.2f", totalWithdrawals));
        Console.info("  Total Transfers: $" + String.format("%.2f", totalTransfers));
        Console.line();

        Console.info("System Overview:");
        Console.info("  Total Transactions: " + transactions.size());
        Console.info("  Total Transaction Volume: $"
                + String.format("%.2f", (totalDeposits + totalWithdrawals + totalTransfers)));
        Console.info("  Net System Change: $" + String.format("%.2f", (totalDeposits - totalWithdrawals)));
        Console.line();
    }

    public void displayCustomerTransactionStatistics(ArrayList<Transaction> transactions, Customer customer) {
        Console.line();
        Console.success("Transaction Statistics for " + customer.getFullName() + ":");
        Console.line();

        long deposits = 0, withdrawals = 0, transfers = 0;
        double totalIn = 0, totalOut = 0;

        for (Transaction t : transactions) {
            switch (t.getTransactionType()) {
                case DEPOSIT:
                    deposits++;
                    totalIn += t.getAmount();
                    break;
                case WITHDRAWAL:
                    withdrawals++;
                    totalOut += t.getAmount();
                    break;
                case TRANSFER:
                    transfers++;
                    boolean isSource = customer.getAccounts().contains(t.getSourceAccount());
                    boolean isDest = customer.getAccounts().contains(t.getDestinationAccount());

                    if (isSource && !isDest) {
                        totalOut += t.getAmount();
                    } else if (!isSource && isDest) {
                        totalIn += t.getAmount();
                    }
                    break;
            }
        }

        Console.info("Customer Transaction Count:");
        Console.info("  Deposits: " + deposits);
        Console.info("  Withdrawals: " + withdrawals);
        Console.info("  Transfers: " + transfers);
        Console.info("  Total: " + transactions.size());
        Console.line();

        Console.info("Customer Financial Summary:");
        Console.info("  Total Money In: +$" + String.format("%.2f", totalIn));
        Console.info("  Total Money Out: -$" + String.format("%.2f", totalOut));
        Console.info("  Net Change: $" + String.format("%.2f", (totalIn - totalOut)));

        double currentBalance = customer.getAccounts().stream().mapToDouble(Account::getBalance).sum();
        Console.info("  Current Total Balance: $" + String.format("%.2f", currentBalance));
        Console.info("  Number of Accounts: " + customer.getAccounts().size());
        Console.line();
    }

    private void displayAccountOverview(List<Account> accounts) {
        Console.info("Account Overview:");
        Console.info("  Total Accounts: " + accounts.size());

        double totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();
        Console.info("  Combined Balance: $" + String.format("%.2f", totalBalance));

        accounts.forEach(account -> Console.info("  " + account.getAccountType() + ": $" +
                String.format("%.2f", account.getBalance())));
    }

    private void displayTransactionOverview(ArrayList<Transaction> allTransactions) {
        Console.line();
        Console.info("Transaction Overview:");
        Console.info("  Total Transactions: " + allTransactions.size());

        if (allTransactions.isEmpty()) {
            Console.info("  No transactions yet.");
            return;
        }

        long deposits = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.DEPOSIT)
                .count();
        long withdrawals = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.WITHDRAWAL)
                .count();
        long transfers = allTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.TRANSFER)
                .count();

        Console.info("  Deposits: " + deposits);
        Console.info("  Withdrawals: " + withdrawals);
        Console.info("  Transfers: " + transfers);
    }

    private void displayFinancialFlowAnalysis(ArrayList<Transaction> allTransactions, Customer customer) {
        Console.line();
        Console.info("Financial Flow Analysis:");

        double totalMoneyIn = 0, totalMoneyOut = 0;

        for (Transaction t : allTransactions) {
            switch (t.getTransactionType()) {
                case DEPOSIT:
                    totalMoneyIn += t.getAmount();
                    break;
                case WITHDRAWAL:
                    totalMoneyOut += t.getAmount();
                    break;
                case TRANSFER:
                    boolean isSource = customer.getAccounts().contains(t.getSourceAccount());
                    boolean isDest = customer.getAccounts().contains(t.getDestinationAccount());

                    if (isSource && !isDest) {
                        totalMoneyOut += t.getAmount();
                    } else if (!isSource && isDest) {
                        totalMoneyIn += t.getAmount();
                    }
                    break;
            }
        }

        Console.info("  Total Money Received: +$" + String.format("%.2f", totalMoneyIn));
        Console.info("  Total Money Spent: -$" + String.format("%.2f", totalMoneyOut));
        Console.info("  Net Financial Change: $" + String.format("%.2f", (totalMoneyIn - totalMoneyOut)));
    }

    private void displayActivityAnalysis(ArrayList<Transaction> allTransactions) {
        Console.line();
        Console.info("Activity Analysis:");
        if (!allTransactions.isEmpty()) {
            double avgAmount = allTransactions.stream()
                    .mapToDouble(Transaction::getAmount)
                    .average()
                    .orElse(0.0);
            Console.info("  Average Transaction Amount: $" + String.format("%.2f", avgAmount));

            double maxAmount = allTransactions.stream()
                    .mapToDouble(Transaction::getAmount)
                    .max()
                    .orElse(0.0);
            Console.info("  Largest Transaction: $" + String.format("%.2f", maxAmount));

            Console.info("  First Transaction Date: " + allTransactions.get(0).getDate());
            Console.info("  Most Recent Transaction: " +
                    allTransactions.get(allTransactions.size() - 1).getDate());
        }
    }
}
