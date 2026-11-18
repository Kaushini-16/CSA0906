package assignment5;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TaskB1 {
    public static long countLinesUnbuffered(Path path) throws IOException {
        long lines = 0;
        try (InputStream in = new FileInputStream(path.toFile())) {
            int b;
            while ((b = in.read()) != -1) if (b == '\n') lines++;
        }
        return lines;
    }
    public static long countLinesBuffered(Path path) throws IOException {
        long lines = 0;
        try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1)
                for (int i = 0; i < n; i++) if (buffer[i] == '\n') lines++;
        }
        return lines;
    }
    private static void generate(Path p, int records) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(p)) {
            for (int i=0; i<records; i++) { w.write("P"+i+",REG"+i+",Name"+i+",7,3800.0"); w.newLine(); }
        }
    }
    public static void main(String[] args) throws Exception {
        Path p = Files.createTempFile("passes", ".csv");
        PassStore store = new PassStore(p);
        store.add("P1001","192311001","Arun",12,3800);
        Files.writeString(p, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        store.add("P1003","192311003","Divya",7,3800);
        System.out.println("Find P1003: " + Arrays.toString(store.find("P1003")));
        System.out.println("Find missing: " + Arrays.toString(store.find("NOPE")));
        System.out.println("Route 7: " + store.listByRoute(7).size() + " valid record(s)");
        System.out.println("Damaged record skipped: " + (store.find("P1003") != null));

        for (int mb : new int[]{1,10,50}) {
            Path test = Files.createTempFile("linecount-"+mb, ".txt");
            try (OutputStream out = Files.newOutputStream(test)) {
                byte[] block = ("sample attendance row\n").getBytes();
                long target = mb * 1024L * 1024L;
                for (long written=0; written<target; written += block.length) out.write(block);
            }
            long t1=System.nanoTime(); long a=countLinesUnbuffered(test); long t2=System.nanoTime();
            long b=countLinesBuffered(test); long t3=System.nanoTime();
            System.out.printf("%d MB: unbuffered=%d lines, %.2f ms; buffered=%d lines, %.2f ms%n",
                    mb,a,(t2-t1)/1e6,b,(t3-t2)/1e6);
            Files.deleteIfExists(test);
        }

        for (int n : new int[]{1000,10000,100000}) {
            generate(p,n);
            long t=System.nanoTime(); store.find("P"+(n-1)); long elapsed=System.nanoTime()-t;
            System.out.printf("Find in %d records: %.3f ms%n",n,elapsed/1e6);
        }
        Files.deleteIfExists(p);
    }
}
