package view;

import controllers.CustomerController;
import models.Customer;
import services.AuthInterface;
import services.AuthService;
import utils.Console;

public class ConsoleUi {
    private final AuthInterface auth;
    private final CustomerController customerController;

    public ConsoleUi() {
        this.auth = AuthService.getInstance();
        this.customerController = new CustomerController();
    }

    public void run() {
        while (true) {
            if (!auth.isAuthenticated()) {
                showAuthMenu();
            } else {
                showProfileMenu();
            }
        }
    }

    private void showAuthMenu() {
        Console.line();
        Console.info("Welcome to the Bank System!");
        Console.info("Please choose an option:");
        Console.info("  1) Register");
        Console.info("  2) Login");
        Console.info("  0) Exit");
        Console.line();

        String opt = Console.ask("Enter choice: ");
        switch (opt) {
            case "0":
                exit(0);
            case "1":
                customerController.register();
                break;
            case "2":
                customerController.login();
                break;
            default:
                Console.error("Invalid option!");
                break;
        }
    }

    private void showProfileMenu() {
        Customer u = auth.requireAuthentication();

        Console.line();
        Console.success("Welcome back, " + u.getFullName() + "!");
        Console.line();

        Console.info("Choose an option:");
        Console.info("  1) Logout");
        Console.info("  2) View Profile");
        Console.info("  3) Update First Name");
        Console.info("  4) Update Last Name");
        Console.info("  5) Update Email");
        Console.info("  6) Update Password");
        Console.info("  0) Exit");
        Console.line();

        String opt = Console.ask("Enter choice: ");
        switch (opt) {
            case "0":
                exit(0);
            case "1":
                customerController.logout();
                break;
            case "2":
                customerController.profile(u);
                break;
            case "3":
                customerController.update("firstName");
                break;
            case "4":
                customerController.update("lastName");
                break;
            case "5":
                customerController.update("email");
                break;
            case "6":
                customerController.update("password");
                break;
            default:
                Console.error("Invalid option!");
                break;
        }
    }

    public static void exit(Integer code) {
        Console.info("Thank you for using the Banking System. Goodbye!");
        Console.close();
        System.exit(code);
    }
}
