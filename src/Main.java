import seeders.Seeder;
import view.ConsoleUi;

public class Main {
    public static void main(String[] args) {
        // Seed the database with initial data
        Seeder.run();

        // Start the console UI
        ConsoleUi ui = new ConsoleUi();
        ui.run();
    }
}