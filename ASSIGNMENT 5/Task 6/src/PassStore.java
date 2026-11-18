package assignment5;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class PassStore {
    private final Path file;
    public PassStore(Path file) { this.file = file; }

    public void add(String passId, String regNo, String name, int route, double amount) throws IOException {
        if (String.join("", passId, regNo, name).contains(",") || name.contains("\n"))
            throw new IllegalArgumentException("Fields may not contain comma/newline in this simple CSV implementation");
        Files.createDirectories(file.toAbsolutePath().getParent());
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join(",", passId, regNo, name, Integer.toString(route), Double.toString(amount)));
            w.newLine();
        }
    }

    public String[] find(String passId) throws IOException {
        if (!Files.exists(file)) return null;
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] fields = parse(line);
                if (fields != null && fields[0].equals(passId)) return fields;
            }
        }
        return null;
    }

    public List<String[]> listByRoute(int route) throws IOException {
        List<String[]> result = new ArrayList<>();
        if (!Files.exists(file)) return result;
        try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] f = parse(line);
                if (f != null && Integer.parseInt(f[3]) == route) result.add(f);
            }
        }
        return result;
    }

    private String[] parse(String line) {
        String[] f = line.split(",", -1);
        if (f.length != 5 || Arrays.stream(f).anyMatch(String::isBlank)) return null;
        try { Integer.parseInt(f[3]); Double.parseDouble(f[4]); return f; }
        catch (NumberFormatException ex) { return null; }
    }
}
