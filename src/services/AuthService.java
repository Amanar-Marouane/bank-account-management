package services;

import java.util.Optional;

import exceptions.AuthenticationFailedException;
import models.Customer;
import models.UserType;
import repositories.CustomerRepository;
import utils.Console;

/**
 * Authentication service implementing user authentication
 * and session management.
 */
public final class AuthService implements AuthInterface {

    private volatile Customer currentUser;
    private volatile boolean authenticated;
    private static volatile AuthService instance;
    private final CustomerRepository customerRepository;

    private AuthService() {
        this.customerRepository = CustomerRepository.getInstance();
        this.authenticated = false;
    }

    public static AuthService getInstance() {
        if (instance == null)
            instance = new AuthService();
        return instance;
    }

    @Override
    public boolean register(String firstName, String lastName, String email, String password) {
        try {
            // Validate current session state
            if (this.authenticated) {
                throw new AuthenticationFailedException("registration",
                        "Active session detected. Please logout first.");
            }

            // Validate input parameters
            if (firstName == null || firstName.trim().isEmpty()) {
                throw new AuthenticationFailedException("registration", "First name cannot be null or empty");
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                throw new AuthenticationFailedException("registration", "Last name cannot be null or empty");
            }
            if (email == null || email.trim().isEmpty()) {
                throw new AuthenticationFailedException("registration", "Email cannot be null or empty");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new AuthenticationFailedException("registration", "Password cannot be null or empty");
            }

            // Check for existing user
            if (customerRepository.find("email", email.toLowerCase().trim()).isPresent()) {
                throw new AuthenticationFailedException("registration", "Email address already registered");
            }

            // Create new customer
            Customer customer = new Customer(
                    sanitizeName(firstName),
                    sanitizeName(lastName),
                    email.toLowerCase().trim(),
                    password,
                    UserType.USER);

            customerRepository.save(customer);
            Console.success("Registration completed successfully! Welcome, " + firstName + "!");
            return true;

        } catch (AuthenticationFailedException e) {
            Console.error("Registration failed: " + e.getReason());
            return false;
        } catch (Exception e) {
            Console.error("Registration failed due to system error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean login(String email, String password) {
        try {
            // Validate current session state
            if (this.authenticated) {
                throw new AuthenticationFailedException("login", "Already authenticated. Please logout first.");
            }

            // Validate input
            if (email == null || email.trim().isEmpty()) {
                throw new AuthenticationFailedException("login", "Email cannot be null or empty");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new AuthenticationFailedException("login", "Password cannot be null or empty");
            }

            // Find user
            Optional<Customer> customerOpt = customerRepository.find("email", email.toLowerCase().trim());
            if (customerOpt.isEmpty()) {
                throw new AuthenticationFailedException("login", "Invalid credentials - user not found");
            }

            Customer customer = customerOpt.get();

            // Verify password
            if (!customer.getPassword().equals(password)) {
                throw new AuthenticationFailedException("login", "Invalid credentials - wrong password");
            }

            // Establish session
            establishSession(customer);
            return true;

        } catch (AuthenticationFailedException e) {
            Console.error("Login failed: Invalid credentials.");
            return false;
        } catch (Exception e) {
            Console.error("Login failed due to system error. Please try again.");
            return false;
        }
    }

    @Override
    public synchronized void logout() {
        if (!this.authenticated) {
            Console.info("No active session to logout.");
            return;
        }

        Console.info("Processing logout...");
        clearSession();
        Console.success("Logout completed successfully!");
    }

    @Override
    public Optional<Customer> getCurrentUser() {
        return this.authenticated ? Optional.ofNullable(this.currentUser) : Optional.empty();
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated && this.currentUser != null;
    }

    @Override
    public Customer requireAuthentication() {
        if (!isAuthenticated()) {
            Console.error("Authentication required. Please login to continue.");
            throw new AuthenticationFailedException("access", "No authenticated user found. Please login first.");
        }
        return this.currentUser;
    }

    @Override
    public void requireNoAuthentication() {
        if (isAuthenticated()) {
            Console.error("Operation not allowed: User already authenticated. Please logout first.");
            throw new AuthenticationFailedException("duplicate", "User is already authenticated. Please logout first.");
        }
    }

    private String sanitizeName(String name) {
        return name.trim().replaceAll("[^a-zA-Z\\s'-]", "");
    }

    private synchronized void establishSession(Customer customer) {
        this.currentUser = customer;
        this.authenticated = true;
    }

    private synchronized void clearSession() {
        this.currentUser = null;
        this.authenticated = false;
    }
}
