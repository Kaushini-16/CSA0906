import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ResultProcessor {
    static class Result {
        final String file;
        final boolean success;
        final String message;
        Result(String file, boolean success, String message) {
            this.file = file; this.success = success; this.message = message;
        }
    }

    static class ScoreTask implements Callable<Result> {
        private final Path path;
        ScoreTask(Path path) { this.path = path; }

        @Override
        public Result call() {
            try {
                String content = Files.readString(path).trim();
                if (content.isEmpty()) throw new IllegalArgumentException("Empty score file");
                int score = Integer.parseInt(content);
                if (score < 0 || score > 100) throw new IllegalArgumentException("Score outside 0-100");
                return new Result(path.getFileName().toString(), true, "Validated score " + score);
            } catch (IOException | NumberFormatException | IllegalArgumentException e) {
                return new Result(path.getFileName().toString(), false, e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Path inputDir = Path.of("scores");
        Files.createDirectories(inputDir);
        // Sample files are created only when they do not already exist.
        createIfMissing(inputDir.resolve("student1.txt"), "85");
        createIfMissing(inputDir.resolve("student2.txt"), "92");
        createIfMissing(inputDir.resolve("student3.txt"), "bad-score");
        // student4.txt is deliberately absent to demonstrate a missing-file failure.

        int poolSize = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        ConcurrentLinkedQueue<Result> results = new ConcurrentLinkedQueue<>();
        List<Future<Result>> futures = new ArrayList<>();

        try {
            for (int i = 1; i <= 4; i++) {
                futures.add(executor.submit(new ScoreTask(inputDir.resolve("student" + i + ".txt"))));
            }
            for (Future<Result> future : futures) results.add(future.get());
        } finally {
            executor.shutdown();
        }

        int successes = 0, failures = 0;
        for (Result result : results) {
            System.out.println(result.file + ": " + (result.success ? "SUCCESS - " : "FAILED - ") + result.message);
            if (result.success) successes++; else failures++;
        }
        System.out.println("Summary: successes=" + successes + ", failures=" + failures);
    }

    private static void createIfMissing(Path path, String content) throws IOException {
        if (!Files.exists(path)) Files.writeString(path, content);
    }
}
