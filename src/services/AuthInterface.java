package services;

import java.util.Optional;

import models.Customer;

public interface AuthInterface {

    /**
     * Registers a new customer with validated credentials.
     * 
     * @param firstName
     * @param lastName
     * @param email
     * @param password
     * @return true if registration successful, false otherwise
     */
    boolean register(String firstName, String lastName, String email, String password);

    /**
     * Authenticates user credentials and establishes a secure session.
     * 
     * @param email
     * @param password
     * @return true if authentication successful, false otherwise
     */
    boolean login(String email, String password);

    /**
     * Terminates the current user session securely.
     */
    void logout();

    /**
     * Retrieves the currently authenticated user.
     * 
     * @return Optional containing the current user, empty if no user is logged in
     */
    Optional<Customer> getCurrentUser();

    /**
     * Checks if a user is currently authenticated.
     * 
     * @return true if user is logged in, false otherwise
     */
    boolean isAuthenticated();

    /**
     * Validates that a user is currently logged in, throws exception if not.
     * 
     * @return the current authenticated user
     * @throws IllegalStateException if no user is logged in
     */
    Customer requireAuthentication();

    /**
     * Validates that no user is currently logged in, throws exception if logged in.
     * 
     * @throws IllegalStateException if a user is currently logged in
     */
    void requireNoAuthentication();
}