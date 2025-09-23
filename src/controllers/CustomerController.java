package controllers;

import java.util.HashMap;

import models.Customer;
import repositories.CustomerRepository;
import services.AuthService;
import utils.Console;

final public class CustomerController {
    private AuthService auth = AuthService.getInstance();
    private CustomerRepository customerRepository = CustomerRepository.getInstance();

    private HashMap<String, String> registerAttempt() {
        HashMap<String, String> registry = new HashMap<>();

        registry.put("email", this.emailAttempt());
        registry.put("firstName", this.nameAttempt("first name"));
        registry.put("lastName", this.nameAttempt("last name"));
        registry.put("password", this.passwordAttempt());

        return registry;
    }

    private HashMap<String, String> loginAttempt() {
        HashMap<String, String> registry = new HashMap<>();

        registry.put("email", this.emailAttempt());
        registry.put("password", this.passwordAttempt());

        return registry;
    }

    private String emailAttempt() {
        String email;
        do {
            email = Console.ask("=> Enter an email");
            if (!isValidEmail(email))
                Console.error("Invalid Email");
        } while (!isValidEmail(email));
        return email;
    }

    private String nameAttempt(String label) {
        String name;
        do {
            name = Console.ask("=> Enter your " + label);
            if (!isValidName(name))
                Console.error("Invalid " + label);
        } while (!isValidName(name));
        return name;
    }

    private String passwordAttempt() {
        String password;
        do {
            password = Console.ask("=> Enter a password");
            if (!isValidPassword(password))
                Console.error("Invalid password");
        } while (!isValidPassword(password));

        return password;
    }

    public void register() {
        Console.info("Processing registration...");

        HashMap<String, String> register = this.registerAttempt();
        String firstName = register.get("firstName");
        String lastName = register.get("lastName");
        String email = register.get("email");
        String password = register.get("password");

        auth.register(firstName, lastName, email, password);
    }

    public void login() {
        Console.info("Processing login...");

        HashMap<String, String> login = this.loginAttempt();
        String email = login.get("email");
        String password = login.get("password");

        if (!isValidEmail(email)) {
            Console.error("Login failed: Invalid email format.");
            return;
        }

        this.auth.login(email, password);
    }

    public void logout() {
        this.auth.logout();
    }

    public void profile(Customer u) {
        Console.line();
        Console.success("=== Your Profile ===");
        Console.info("User ID   : " + u.getId());
        Console.info("Full Name : " + u.getFullName());
        Console.info("Email     : " + u.getEmail());
        Console.line();
    }

    public void update(String key) {
        Customer u = this.auth.getCurrentUser().get();
        Console.line();
        Console.info("Updating your " + key + "...");

        switch (key.toLowerCase()) {
            case "email":
                String oldEmail = u.getEmail();
                String email = this.emailAttempt();

                // Check if the new email is the same as the old one
                if (email.equals(oldEmail)) {
                    Console.warning("The new email is the same as your current one.");
                    return;
                }

                // Check if email already exists
                if (customerRepository.find("email", email) != null) {
                    Console.error("This email is already registered. Please use a different one.");
                    return;
                }

                u.setEmail(email);
                customerRepository.save(u);
                Console.success("Your email has been updated successfully.");
                break;
            case "firstname":
                String firstName = this.nameAttempt("first name");
                u.setFirstName(firstName);
                Console.success("Your first name has been updated successfully.");
                break;

            case "lastname":
                String lastName = this.nameAttempt("last name");
                u.setLastName(lastName);
                Console.success("Your last name has been updated successfully.");
                break;

            case "password":
                String password = this.passwordAttempt();
                if (u.getPassword().equals(password)) {
                    Console.warning("The new password cannot be the same as the old one.");
                    return;
                }
                u.setPassword(password);
                Console.success("Your password has been updated successfully.");
                break;

            case "exit":
                Console.info("Update aborted by user.");
                break;

            default:
                Console.warning("Property chosen is not fillable. Type 'exit' to cancel.");
                break;
        }
        Console.line();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}
