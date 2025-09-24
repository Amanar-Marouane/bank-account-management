package seeders;

import models.Account;
import models.AccountType;
import models.Customer;
import models.Transaction;
import models.TransactionType;
import models.UserType;
import repositories.AccountRepository;
import repositories.CustomerRepository;
import repositories.TransactionRepository;
import utils.Console;

public class Seeder {
        public static void run() {
                // Get repository instances
                CustomerRepository customerRepo = CustomerRepository.getInstance();
                AccountRepository accountRepo = AccountRepository.getInstance();
                TransactionRepository transactionRepo = TransactionRepository.getInstance();

                // Create admin-only customer (no accounts)
                Customer adminCustomer = new Customer("Marouane", "Amanar", "marouane@gmail.com", "mmMM00!!",
                                UserType.ADMIN);
                customerRepo.save(adminCustomer);

                // Create first regular customer
                Customer customer1 = new Customer("Omar", "Ouyacho", "omar@gmail.com", "llLL00!!", UserType.USER);
                customerRepo.save(customer1);

                // Create second regular customer
                Customer customer2 = new Customer("Sarah", "Johnson", "sarah@gmail.com", "ssSS00!!", UserType.USER);
                customerRepo.save(customer2);

                // Create accounts for customer1
                Account checking1 = new Account(AccountType.CHECKING, customer1);
                Account savings1 = new Account(AccountType.SAVINGS, customer1);
                accountRepo.save(checking1);
                accountRepo.save(savings1);

                // Create accounts for customer2
                Account checking2 = new Account(AccountType.CHECKING, customer2);
                Account savings2 = new Account(AccountType.SAVINGS, customer2);
                accountRepo.save(checking2);
                accountRepo.save(savings2);

                // Create transactions

                // Customer 1 deposits
                Transaction deposit1 = new Transaction(
                                TransactionType.DEPOSIT,
                                1000.0,
                                "Initial deposit",
                                checking1,
                                checking1);
                transactionRepo.save(deposit1);

                Transaction deposit2 = new Transaction(
                                TransactionType.DEPOSIT,
                                500.0,
                                "Savings deposit",
                                savings1,
                                savings1);
                transactionRepo.save(deposit2);

                // Customer 2 deposits
                Transaction deposit3 = new Transaction(
                                TransactionType.DEPOSIT,
                                750.0,
                                "Paycheck deposit",
                                checking2,
                                checking2);
                transactionRepo.save(deposit3);

                Transaction deposit4 = new Transaction(
                                TransactionType.DEPOSIT,
                                300.0,
                                "Birthday money",
                                savings2,
                                savings2);
                transactionRepo.save(deposit4);

                // Withdrawals
                Transaction withdrawal1 = new Transaction(
                                TransactionType.WITHDRAWAL,
                                200.0,
                                "ATM withdrawal",
                                checking1,
                                checking1);
                transactionRepo.save(withdrawal1);

                Transaction withdrawal2 = new Transaction(
                                TransactionType.WITHDRAWAL,
                                100.0,
                                "Cash withdrawal",
                                checking2,
                                checking2);
                transactionRepo.save(withdrawal2);

                // Transfer between customer1's accounts
                Transaction transfer1 = new Transaction(
                                TransactionType.TRANSFER,
                                150.0,
                                "Transfer to savings",
                                checking1,
                                savings1);
                transactionRepo.save(transfer1);

                // Transfer between customers
                Transaction transfer2 = new Transaction(
                                TransactionType.TRANSFER,
                                50.0,
                                "Payment to Sarah",
                                checking1,
                                checking2);
                transactionRepo.save(transfer2);

                Console.info("Database seeded successfully!");
                Console.info("Created 1 admin user and 2 regular customers with accounts and transactions.");
        }
}