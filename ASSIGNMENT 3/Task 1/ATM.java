import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ATM {
    private static final double BALANCE = 5000.00;

    static void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > BALANCE) {
            throw new ArithmeticException("Insufficient account balance.");
        }
        System.out.printf("Withdrawal successful. Remaining balance: %.2f%n", BALANCE - amount);
    }

    public static void main(String[] args) {
        // A missing card-reader file is a checked IOException.
        try {
            if (!Files.exists(Path.of("card.txt"))) {
                throw new IOException("Card-reader file is missing.");
            }
            try (Scanner input = new Scanner(System.in)) {
                System.out.print("Enter account number: ");
                String account = input.nextLine().trim();
                if (account.isEmpty()) throw new IllegalArgumentException("Account number cannot be empty.");

                System.out.print("Enter withdrawal amount: ");
                double amount = input.nextDouble();
                withdraw(amount);
            }
        } catch (InputMismatchException | NumberFormatException e) {
            System.out.println("Input error: enter a valid number.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid input: " + e.getMessage());
        } catch (ArithmeticException e) {
            System.out.println("Transaction declined: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        } finally {
            System.out.println("ATM session ended. Please collect your card.");
        }
    }
}
