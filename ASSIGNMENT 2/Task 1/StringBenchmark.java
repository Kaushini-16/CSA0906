import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.function.Function;

/** Q1 - String (+), StringBuilder, StringBuffer (and join/StringJoiner/format) under three workloads. */
public class StringBenchmark {
    static int sink;                                   // prevents dead-code elimination
    static final com.sun.management.ThreadMXBean MX =
            (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    static String concat(String[] f) { String s = ""; for (String x : f) s += x + "|"; return s; }
    static String builder(String[] f) { StringBuilder sb = new StringBuilder(); for (String x : f) sb.append(x).append('|'); return sb.toString(); }
    static String buffer(String[] f) { StringBuffer sb = new StringBuffer(); for (String x : f) sb.append(x).append('|'); return sb.toString(); }
    static String join(String[] f) { return String.join("|", f); }
    static String joiner(String[] f) { StringJoiner j = new StringJoiner("|"); for (String x : f) j.add(x); return j.toString(); }
    static String format(String[] f) { StringBuilder sb = new StringBuilder(); for (String x : f) sb.append(String.format("%s|", x)); return sb.toString(); }

    static String[] fields(int n) {
        String[] f = new String[n];
        for (int i = 0; i < n; i++) f[i] = "F" + i + "=value" + (i * 37 % 1000);
        return f;
    }

    /** Returns {avg ms, allocated MB per run} */
    static double[] measure(Function<String[], String> gen, String[] f, int tickets) {
        for (int w = 0; w < 2; w++) for (int t = 0; t < Math.max(1, tickets / 10); t++) sink += gen.apply(f).length();   // warm-up
        int runs = 5; double totalMs = 0; double totalMb = 0;
        for (int r = 0; r < runs; r++) {
            long a0 = MX.getThreadAllocatedBytes(Thread.currentThread().getId());
            long t0 = System.nanoTime();
            for (int t = 0; t < tickets; t++) sink += gen.apply(f).length();
            totalMs += (System.nanoTime() - t0) / 1e6;
            totalMb += (MX.getThreadAllocatedBytes(Thread.currentThread().getId()) - a0) / 1048576.0;
        }
        return new double[]{totalMs / runs, totalMb / runs};
    }

    public static void main(String[] args) {
        int[][] workloads = {{8, 100_000}, {50, 10_000}, {2_000, 50}};   // {fields per ticket, tickets}
        String[] names = {"A: short ticket (8 fields x 100,000 tickets)", "B: 50 fields x 10,000 tickets", "C: own - long ticket (2,000 fields x 50 tickets)"};
        String[] approach = {"String +", "StringBuilder", "StringBuffer", "String.join", "StringJoiner", "String.format"};
        List<Function<String[], String>> gens = List.of(StringBenchmark::concat, StringBenchmark::builder, StringBenchmark::buffer,
                StringBenchmark::join, StringBenchmark::joiner, StringBenchmark::format);
        StringBuilder csv = new StringBuilder("workload,approach,avg_ms,alloc_MB_per_run,est_intermediate_strings,est_chars_copied\n");
        for (int w = 0; w < workloads.length; w++) {
            int n = workloads[w][0], tickets = workloads[w][1];
            String[] f = fields(n);
            int avgLen = concat(f).length() / n;                     // average chars added per field
            System.out.println("\n== " + names[w]);
            System.out.printf("%-14s %12s %16s %22s %18s%n", "Approach", "avg ms", "alloc MB/run", "est. temp Strings", "est. chars copied");
            for (int a = 0; a < approach.length; a++) {
                double[] m = measure(gens.get(a), f, tickets);
                long tempStrings = a == 0 ? (long) tickets * n : tickets;               // + : one new String per field
                long charsCopied = a == 0 ? (long) tickets * avgLen * n * (n + 1L) / 2 : (long) tickets * avgLen * n * 2; // O(n^2) vs ~O(n)
                System.out.printf("%-14s %12.2f %16.1f %22d %18d%n", approach[a], m[0], m[1], tempStrings, charsCopied);
                csv.append(String.format("\"%s\",%s,%.3f,%.2f,%d,%d%n", names[w], approach[a], m[0], m[1], tempStrings, charsCopied));
            }
        }
        try { java.nio.file.Files.writeString(java.nio.file.Path.of("raw_data.csv"), csv.toString()); } catch (Exception e) { }
        System.out.println("\n(sink=" + sink + ")  raw data written to raw_data.csv");
    }
}
