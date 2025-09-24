package view;

import controllers.AdminController;
import controllers.CustomerController;
import models.Customer;
import models.UserType;
import services.AuthInterface;
import services.AuthService;
import utils.Console;

public class ConsoleUi {
    private final AuthInterface auth;
    private final CustomerController customerController;
    private final AdminController adminController;

    public ConsoleUi() {
        this.auth = AuthService.getInstance();
        this.customerController = new CustomerController(auth);
        this.adminController = new AdminController(auth);
    }

    public void run() {
        while (true) {
            if (!auth.isAuthenticated()) {
                showAuthMenu();
            } else {
                Customer user = auth.requireAuthentication();
                if (user.getUserType() == UserType.ADMIN) {
                    showAdminMenu();
                } else {
                    showUserMenu();
                }
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

    private void showAdminMenu() {
        Customer u = auth.requireAuthentication();

        Console.line();
        Console.success("Welcome back, Admin " + u.getFullName() + "!");
        Console.line();

        Console.info("Admin Panel - Choose an option:");
        Console.info("  1) Manage Customers");
        Console.info("  2) View Profile");
        Console.info("  3) Update Profile");
        Console.info("  4) Logout");
        Console.info("  0) Exit");
        Console.line();

        String opt = Console.ask("Enter choice: ");
        switch (opt) {
            case "0":
                exit(0);
            case "1":
                adminController.manageCustomers();
                break;
            case "2":
                customerController.profile(u);
                break;
            case "3":
                showUpdateProfileMenu();
                break;
            case "4":
                customerController.logout();
                break;
            default:
                Console.error("Invalid option!");
                break;
        }
    }

    private void showUserMenu() {
        Customer u = auth.requireAuthentication();

        Console.line();
        Console.success("Welcome back, " + u.getFullName() + "!");
        Console.line();

        Console.info("Choose an option:");
        Console.info("  1) Logout");
        Console.info("  2) View Profile");
        Console.info("  3) Update Profile");
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
                showUpdateProfileMenu();
                break;
            default:
                Console.error("Invalid option!");
                break;
        }
    }

    private void showUpdateProfileMenu() {
        while (true) {
            Console.line();
            Console.info("Update Profile:");
            Console.info("  1) Update First Name");
            Console.info("  2) Update Last Name");
            Console.info("  3) Update Email");
            Console.info("  4) Update Password");
            Console.info("  0) Back");
            Console.line();

            String opt = Console.ask("Enter choice: ");
            switch (opt) {
                case "0":
                    return;
                case "1":
                    customerController.update("firstName");
                    break;
                case "2":
                    customerController.update("lastName");
                    break;
                case "3":
                    customerController.update("email");
                    break;
                case "4":
                    customerController.update("password");
                    break;
                default:
                    Console.error("Invalid option!");
                    break;
            }
        }
    }

    public static void exit(Integer code) {
        Console.info("Thank you for using the Banking System. Goodbye!");
        Console.close();
        System.exit(code);
    }
}
