import seeders.Seeder;
import utils.Console;
import view.ConsoleUi;

class Main {

    public static void main(String[] args) {
        // Initialize application & seed data
        Console.info("Welcome to the Banking System!");
        Console.info("Initializing application...");
        Seeder.run();
        Console.info("Application started successfully.");

        // Create and run the console UI
        ConsoleUi ui = new ConsoleUi();
        ui.run();
    }
}