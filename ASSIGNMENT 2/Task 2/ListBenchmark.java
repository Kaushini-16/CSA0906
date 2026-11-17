import java.util.*;

/** Q2 - ArrayList vs LinkedList vs ArrayDeque vs manual array for a playlist. */
public class ListBenchmark {
    record Song(int id) {}
    static long sink;

    interface Ops {
        void addLast(Song s); void addFirst(Song s); void insertMid(Song s);
        Song removeFirst(); void removeMid(); Song get(int k); long iterate(); int size();
    }
    static class ListOps implements Ops {
        final List<Song> l; ListOps(List<Song> l) { this.l = l; }
        public void addLast(Song s) { l.add(s); } public void addFirst(Song s) { l.add(0, s); }
        public void insertMid(Song s) { l.add(l.size() / 2, s); }
        public Song removeFirst() { return l.remove(0); } public void removeMid() { l.remove(l.size() / 2); }
        public Song get(int k) { return l.get(k); }
        public long iterate() { long x = 0; for (Song s : l) x += s.id(); return x; } public int size() { return l.size(); }
    }
    static class DequeOps implements Ops {
        final ArrayDeque<Song> d = new ArrayDeque<>();
        public void addLast(Song s) { d.addLast(s); } public void addFirst(Song s) { d.addFirst(s); }
        public void insertMid(Song s) { throw new UnsupportedOperationException(); }
        public Song removeFirst() { return d.removeFirst(); } public void removeMid() { throw new UnsupportedOperationException(); }
        public Song get(int k) { throw new UnsupportedOperationException(); }
        public long iterate() { long x = 0; for (Song s : d) x += s.id(); return x; } public int size() { return d.size(); }
    }
    static class ArrayOps implements Ops {            // plain array with manual shifting (grows by doubling)
        Song[] a = new Song[16]; int n;
        void grow() { if (n == a.length) a = Arrays.copyOf(a, n * 2); }
        public void addLast(Song s) { grow(); a[n++] = s; }
        public void addFirst(Song s) { insertAt(0, s); }
        public void insertMid(Song s) { insertAt(n / 2, s); }
        void insertAt(int i, Song s) { grow(); for (int j = n; j > i; j--) a[j] = a[j - 1]; a[i] = s; n++; }
        public Song removeFirst() { return removeAt(0); } public void removeMid() { removeAt(n / 2); }
        Song removeAt(int i) { Song r = a[i]; for (int j = i; j < n - 1; j++) a[j] = a[j + 1]; a[--n] = null; return r; }
        public Song get(int k) { return a[k]; }
        public long iterate() { long x = 0; for (int i = 0; i < n; i++) x += a[i].id(); return x; } public int size() { return n; }
    }

    static Ops make(String type, int n) {
        Ops o = switch (type) {
            case "ArrayList" -> new ListOps(new ArrayList<>()); case "LinkedList" -> new ListOps(new LinkedList<>());
            case "ArrayDeque" -> new DequeOps(); default -> new ArrayOps();
        };
        for (int i = 0; i < n; i++) o.addLast(new Song(i));
        return o;
    }

    static final int OPS = 1000, RUNS = 5;
    static final String[] TYPES = {"ArrayList", "LinkedList", "ArrayDeque", "Array(manual)"};
    static final String[] OPNAMES = {"add at end (per op)", "insert at front", "insert in middle", "remove front", "remove middle", "get k-th (random)", "iterate whole list (total ms)"};

    /** average time per operation in microseconds (iterate: total ms), or NaN if unsupported */
    static double run(String type, int n, int op) {
        double total = 0;
        for (int r = 0; r < RUNS + 1; r++) {                         // first run is warm-up
            Ops o = make(type, n); Random rnd = new Random(42); long t0, t1;
            try {
                switch (op) {
                    case 0 -> { Ops fresh = make(type, 0); t0 = System.nanoTime(); for (int i = 0; i < n; i++) fresh.addLast(new Song(i)); t1 = System.nanoTime(); total += r == 0 ? 0 : (t1 - t0) / 1e3 / n; continue; }
                    case 1 -> { t0 = System.nanoTime(); for (int i = 0; i < OPS; i++) o.addFirst(new Song(i)); }
                    case 2 -> { t0 = System.nanoTime(); for (int i = 0; i < OPS; i++) o.insertMid(new Song(i)); }
                    case 3 -> { t0 = System.nanoTime(); for (int i = 0; i < OPS; i++) sink += o.removeFirst().id(); }
                    case 4 -> { t0 = System.nanoTime(); for (int i = 0; i < OPS; i++) o.removeMid(); }
                    case 5 -> { int[] ks = new int[OPS]; for (int i = 0; i < OPS; i++) ks[i] = rnd.nextInt(n); t0 = System.nanoTime(); for (int i = 0; i < OPS; i++) sink += o.get(ks[i]).id(); }
                    default -> { t0 = System.nanoTime(); sink += o.iterate(); t1 = System.nanoTime(); total += r == 0 ? 0 : (t1 - t0) / 1e6; continue; }
                }
                t1 = System.nanoTime();
                total += r == 0 ? 0 : (t1 - t0) / 1e3 / OPS;
            } catch (UnsupportedOperationException e) { return Double.NaN; }
        }
        return total / RUNS;
    }

    public static void main(String[] args) {
        StringBuilder csv = new StringBuilder("size,operation,ArrayList,LinkedList,ArrayDeque,ArrayManual\n");
        for (int n : new int[]{10_000, 50_000, 100_000}) {
            System.out.println("\n== Playlist size n = " + n + "   (microseconds per operation; iterate = ms total)");
            System.out.printf("%-32s %12s %12s %12s %14s%n", "Operation", "ArrayList", "LinkedList", "ArrayDeque", "Array(manual)");
            for (int op = 0; op < OPNAMES.length; op++) {
                double[] v = new double[4];
                for (int t = 0; t < 4; t++) v[t] = run(TYPES[t], n, op);
                System.out.printf("%-32s %12s %12s %12s %14s%n", OPNAMES[op], f(v[0]), f(v[1]), f(v[2]), f(v[3]));
                csv.append(n).append(',').append(OPNAMES[op]).append(',').append(f(v[0])).append(',').append(f(v[1])).append(',').append(f(v[2])).append(',').append(f(v[3])).append('\n');
            }
        }
        // Changed condition: "Up Next" queue - add at both ends, remove from the front
        System.out.println("\n== Up Next queue: 200,000 mixed operations (add front, add back, remove front) on a queue of 5,000 songs; total ms");
        for (String type : new String[]{"ArrayList", "LinkedList", "ArrayDeque"}) {
            double tot = 0;
            for (int r = 0; r < 4; r++) {
                Ops o = make(type, 5000); long t0 = System.nanoTime();
                for (int i = 0; i < 200_000; i++) { int m = i % 3; if (m == 0) o.addFirst(new Song(i)); else if (m == 1) o.addLast(new Song(i)); else sink += o.removeFirst().id(); }
                if (r > 0) tot += (System.nanoTime() - t0) / 1e6;
            }
            System.out.printf("%-12s %10.2f ms%n", type, tot / 3);
            csv.append("UpNext,").append(type).append(',').append(String.format("%.2f", tot / 3)).append('\n');
        }
        try { java.nio.file.Files.writeString(java.nio.file.Path.of("raw_data.csv"), csv.toString()); } catch (Exception e) { }
        System.out.println("\n(sink=" + sink + ")  raw data written to raw_data.csv");
    }
    static String f(double d) { return Double.isNaN(d) ? "n/a" : String.format("%.3f", d); }
}
