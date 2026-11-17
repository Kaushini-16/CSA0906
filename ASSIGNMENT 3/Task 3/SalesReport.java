import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SalesReport {
    private static final List<String> INPUTS = List.of("store1.txt", "store2.txt");

    // Safer implementation: every opened resource is closed automatically.
    static void generateWithTryResources() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of("sales-report.txt"))) {
            for (String file : INPUTS) {
                System.out.println("Reading " + file);
                try (BufferedReader reader = Files.newBufferedReader(Path.of(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                } catch (IOException e) {
                    System.err.println("Failed while reading " + file + ": " + e.getMessage());
                    throw e;
                }
            }
        }
    }

    // Demonstration of explicit close in finally. Try-with-resources is preferred.
    static void generateManually() throws IOException {
        BufferedWriter writer = null;
        try {
            writer = Files.newBufferedWriter(Path.of("manual-report.txt"));
            for (String file : INPUTS) {
                BufferedReader reader = null;
                try {
                    reader = Files.newBufferedReader(Path.of(file));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                } finally {
                    if (reader != null) reader.close();
                }
            }
        } finally {
            if (writer != null) writer.close();
        }
    }

    public static void main(String[] args) {
        try {
            generateWithTryResources();
            System.out.println("Report generated: sales-report.txt");
        } catch (IOException e) {
            System.out.println("Report incomplete: " + e.getMessage());
        }
    }
}
