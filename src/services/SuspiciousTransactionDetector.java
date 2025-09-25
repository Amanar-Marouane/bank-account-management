package services;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import exceptions.SuspiciousActivityException;
import models.Transaction;

public class SuspiciousTransactionDetector {
    private static final double HIGH_VALUE_LIMIT = 10000;
    private static final int RAPID_TRANSACTION_COUNT = 5;
    private static final long RAPID_TRANSACTION_WINDOW_MS = 60000; // 1 minute
    private static final int REPETITIVE_THRESHOLD = 8;

    public void validateTransaction(List<Transaction> transactions, Transaction newTransaction)
            throws SuspiciousActivityException {

        if (isHighValue(newTransaction)) {
            throw new SuspiciousActivityException("HIGH_VALUE_TRANSACTION",
                    String.format("Transaction amount $%.2f exceeds high value threshold of $%.2f",
                            newTransaction.getAmount(), HIGH_VALUE_LIMIT));
        }

        if (isBurst(transactions, newTransaction)) {
            throw new SuspiciousActivityException("RAPID_TRANSACTIONS",
                    String.format("More than %d transactions detected within %d seconds",
                            RAPID_TRANSACTION_COUNT, RAPID_TRANSACTION_WINDOW_MS / 1000));
        }

        if (isRepetitive(transactions, newTransaction)) {
            throw new SuspiciousActivityException("REPETITIVE_PATTERN",
                    String.format("Repetitive transaction pattern detected (threshold: %d)",
                            REPETITIVE_THRESHOLD));
        }
    }

    public boolean detectSuspicious(List<Transaction> transactions, Transaction newTransaction) {
        return isHighValue(newTransaction) ||
                isBurst(transactions, newTransaction) ||
                isRepetitive(transactions, newTransaction);
    }

    private boolean isHighValue(Transaction t) {
        return t.getAmount() > HIGH_VALUE_LIMIT;
    }

    private boolean isBurst(List<Transaction> txs, Transaction newTransaction) {
        List<Transaction> allTxs = new ArrayList<>(txs);
        allTxs.add(newTransaction);
        allTxs.sort(Comparator.comparing(Transaction::getDateTime));

        if (allTxs.size() < RAPID_TRANSACTION_COUNT)
            return false;

        for (int i = 0; i <= allTxs.size() - RAPID_TRANSACTION_COUNT; i++) {
            Transaction startTx = allTxs.get(i);
            Transaction endTx = allTxs.get(i + RAPID_TRANSACTION_COUNT - 1);

            long diffMillis = Duration.between(startTx.getDateTime(), endTx.getDateTime()).toMillis();
            if (diffMillis <= RAPID_TRANSACTION_WINDOW_MS) {
                return true;
            }

        }
        return false;
    }

    private boolean isRepetitive(List<Transaction> txs, Transaction newTransaction) {
        List<Transaction> allTxs = new ArrayList<>(txs);
        allTxs.add(newTransaction);
        allTxs.sort(Comparator.comparing(Transaction::getDateTime));

        if (allTxs.size() < REPETITIVE_THRESHOLD)
            return false;

        HashMap<String, Integer> activities = new HashMap<>();

        allTxs.stream().forEach(t -> {
            String key = t.getTransactionType() + ":" + t.getAmount() + ":"
                    + (t.getDestinationAccount() != null ? t.getDestinationAccount().getId() : "");
            activities.put(key, activities.getOrDefault(key, 0) + 1);
        });

        for (int count : activities.values()) {
            if (count >= REPETITIVE_THRESHOLD) {
                return true;
            }
        }

        return false;
    }
}
