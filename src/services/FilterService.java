package services;

import models.Customer;
import models.Transaction;
import models.TransactionType;
import utils.Console;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class FilterService {
    private static FilterService instance;

    private FilterService() {
    }

    public static FilterService getInstance() {
        if (instance == null) {
            instance = new FilterService();
        }
        return instance;
    }

    public ArrayList<Transaction> filterTransactions(ArrayList<Transaction> transactions) {
        Console.line();
        Console.info("Filter Transactions:");
        Console.info("1) By Transaction Type");
        Console.info("2) By Description");
        Console.info("3) By Date Range");
        Console.info("4) By Amount Range");
        Console.info("0) No Filter (Show All)");
        Console.line();

        String field = Console.ask("Enter your choice: ");
        switch (field) {
            case "0":
                return transactions;
            case "1":
                return filterByTransactionType(transactions);
            case "2":
                return filterByDescription(transactions);
            case "3":
                return filterByDateRange(transactions);
            case "4":
                return filterByAmountRange(transactions);
            default:
                Console.warning("Invalid choice. Showing all transactions.");
                return transactions;
        }
    }

    public ArrayList<Transaction> sortTransactions(ArrayList<Transaction> transactions) {
        Console.line();
        Console.info("Sort Transactions:");
        Console.info("1) Sort by Date (Newest First)");
        Console.info("2) Sort by Date (Oldest First)");
        Console.info("3) Sort by Amount (Highest First)");
        Console.info("4) Sort by Amount (Lowest First)");
        Console.info("5) Sort by Type");
        Console.info("0) Cancel");
        Console.line();

        String choice = Console.ask("Enter sorting option: ");
        ArrayList<Transaction> sortedTransactions = new ArrayList<>(transactions);

        switch (choice) {
            case "0":
                return transactions;
            case "1":
                sortedTransactions.sort((t1, t2) -> t2.getDate().compareTo(t1.getDate()));
                break;
            case "2":
                sortedTransactions.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));
                break;
            case "3":
                sortedTransactions.sort((t1, t2) -> Double.compare(t2.getAmount(), t1.getAmount()));
                break;
            case "4":
                sortedTransactions.sort((t1, t2) -> Double.compare(t1.getAmount(), t2.getAmount()));
                break;
            case "5":
                sortedTransactions.sort((t1, t2) -> t1.getTransactionType().compareTo(t2.getTransactionType()));
                break;
            default:
                Console.error("Invalid sorting option!");
                return transactions;
        }
        return sortedTransactions;
    }

    private ArrayList<Transaction> filterByTransactionType(ArrayList<Transaction> transactions) {
        Console.line();
        Console.info("Select Transaction Type:");
        TransactionType[] types = TransactionType.values();
        for (int i = 0; i < types.length; i++) {
            Console.info((i + 1) + ") " + types[i]);
        }

        String choice = Console.ask("Enter transaction type number: ");
        try {
            int typeIndex = Integer.parseInt(choice) - 1;
            if (typeIndex >= 0 && typeIndex < types.length) {
                TransactionType selectedType = types[typeIndex];
                return transactions.stream()
                        .filter(t -> t.getTransactionType() == selectedType)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            }
        } catch (NumberFormatException e) {
            Console.error("Invalid input format.");
        }
        return new ArrayList<>();
    }

    private ArrayList<Transaction> filterByDescription(ArrayList<Transaction> transactions) {
        String description = Console.ask("Enter keyword to search in descriptions: ");
        if (description.trim().isEmpty()) {
            Console.warning("Empty search term. Showing all transactions.");
            return transactions;
        }

        return transactions.stream()
                .filter(t -> t.getDescription().toLowerCase().contains(description.toLowerCase()))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private ArrayList<Transaction> filterByDateRange(ArrayList<Transaction> transactions) {
        try {
            String startDate = Console.ask("Enter start date (yyyy-MM-dd) or press Enter for no start limit: ");
            String endDate = Console.ask("Enter end date (yyyy-MM-dd) or press Enter for no end limit: ");

            LocalDate start = startDate.trim().isEmpty() ? null
                    : LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate end = endDate.trim().isEmpty() ? null
                    : LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);

            return transactions.stream()
                    .filter(t -> {
                        LocalDate transactionDate = t.getDate();
                        boolean afterStart = start == null || !transactionDate.isBefore(start);
                        boolean beforeEnd = end == null || !transactionDate.isAfter(end);
                        return afterStart && beforeEnd;
                    })
                    .collect(Collectors.toCollection(ArrayList::new));

        } catch (DateTimeParseException e) {
            Console.error("Invalid date format. Please use yyyy-MM-dd format.");
            return new ArrayList<>();
        }
    }

    private ArrayList<Transaction> filterByAmountRange(ArrayList<Transaction> transactions) {
        try {
            String minAmount = Console.ask("Enter minimum amount or press Enter for no minimum: ");
            String maxAmount = Console.ask("Enter maximum amount or press Enter for no maximum: ");

            Double min = minAmount.trim().isEmpty() ? null : Double.parseDouble(minAmount);
            Double max = maxAmount.trim().isEmpty() ? null : Double.parseDouble(maxAmount);

            return transactions.stream()
                    .filter(t -> {
                        double amount = t.getAmount();
                        boolean aboveMin = min == null || amount >= min;
                        boolean belowMax = max == null || amount <= max;
                        return aboveMin && belowMax;
                    })
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        } catch (NumberFormatException e) {
            Console.error("Invalid amount format. Please enter valid numbers.");
            return new ArrayList<>();
        }
    }

    public void displayFilteredTransactions(ArrayList<Transaction> transactions, Customer customer) {
        Console.line();
        Console.success("Your Transactions (" + transactions.size() + " results):");
        Console.line();

        double totalIn = 0, totalOut = 0;

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            String accountInfo = "";
            String sign = "";
            double effectiveAmount = t.getAmount();

            // Determine which account and direction
            if (t.getTransactionType() == TransactionType.DEPOSIT) {
                sign = "+";
                totalIn += effectiveAmount;
                accountInfo = "[" + t.getDestinationAccount().getAccountType() + "]";
            } else if (t.getTransactionType() == TransactionType.WITHDRAWAL) {
                sign = "-";
                totalOut += effectiveAmount;
                accountInfo = "[" + t.getSourceAccount().getAccountType() + "]";
            } else { // TRANSFER
                boolean isSource = customer.getAccounts().contains(t.getSourceAccount());
                boolean isDest = customer.getAccounts().contains(t.getDestinationAccount());

                if (isSource && isDest) {
                    // Internal transfer between my own accounts
                    accountInfo = "[" + t.getSourceAccount().getAccountType() + " → " +
                            t.getDestinationAccount().getAccountType() + "]";
                    sign = "↔";
                } else if (isSource) {
                    // Outgoing transfer to someone else
                    sign = "-";
                    totalOut += effectiveAmount;
                    accountInfo = "[" + t.getSourceAccount().getAccountType() + " → External]";
                } else {
                    // Incoming transfer from someone else
                    sign = "+";
                    totalIn += effectiveAmount;
                    accountInfo = "[External → " + t.getDestinationAccount().getAccountType() + "]";
                }
            }

            Console.info((i + 1) + ") " + t.getTransactionType() + " " + accountInfo + " | " +
                    sign + "$" + String.format("%.2f", effectiveAmount) + " | " +
                    t.getDescription() + " | " + t.getDate());
        }

        Console.line();
        Console.success("Summary:");
        Console.info("Total Money In: +$" + String.format("%.2f", totalIn));
        Console.info("Total Money Out: -$" + String.format("%.2f", totalOut));
        Console.info("Net Change: $" + String.format("%.2f", (totalIn - totalOut)));
        Console.line();
    }

}
