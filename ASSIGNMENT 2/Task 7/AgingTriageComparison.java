import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * Q7 - Diagnose, redesign, validate.
 * ORIGINAL : Q6 PriorityQueue + aging by rebuilding the whole queue on every serve (priority depends on time).
 * REDESIGN : five FIFO queues (one per severity). Each queue is already in arrival order, so the OLDEST patient of a
 *            level (its head) always has the best aged priority of that level -> only 5 heads are compared per serve.
 */
public class AgingTriageComparison {
    record Patient(String name, int severity, long arrival) {}
    static final int STEP = 50;                                            // aging: one level per 50 time units
    static int effective(Patient p, long now) { return Math.max(1, p.severity() - (int) ((now - p.arrival()) / STEP)); }
    static long sink;
    static final com.sun.management.ThreadMXBean MX = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    interface Triage { void admit(Patient p); Patient serve(long now); long comparisons(); }

    /** ORIGINAL design */
    static class RebuildTriage implements Triage {
        private final List<Patient> waiting = new ArrayList<>(); private long cmp;
        public void admit(Patient p) { waiting.add(p); }
        public Patient serve(long now) {
            if (waiting.isEmpty()) return null;
            PriorityQueue<Patient> pq = new PriorityQueue<>((a, b) -> {                  // rebuilt every time: O(n log n)
                cmp++; int c = Integer.compare(effective(a, now), effective(b, now));
                return c != 0 ? c : Long.compare(a.arrival(), b.arrival()); });
            pq.addAll(waiting);
            Patient next = pq.poll(); waiting.remove(next);                              // remove from list: O(n)
            return next;
        }
        public long comparisons() { return cmp; }
    }

    /** REDESIGN */
    static class LevelQueuesTriage implements Triage {
        @SuppressWarnings("unchecked") private final ArrayDeque<Patient>[] q = new ArrayDeque[6]; private long cmp;
        LevelQueuesTriage() { for (int i = 1; i <= 5; i++) q[i] = new ArrayDeque<>(); }
        public void admit(Patient p) { q[p.severity()].addLast(p); }                    // O(1)
        public Patient serve(long now) {
            int best = -1;
            for (int lvl = 1; lvl <= 5; lvl++) {                                         // at most 5 head comparisons: O(1)
                Patient h = q[lvl].peekFirst(); if (h == null) continue;
                if (best < 0) { best = lvl; continue; }
                cmp++; Patient b = q[best].peekFirst();
                int c = Integer.compare(effective(h, now), effective(b, now));
                if (c < 0 || (c == 0 && h.arrival() < b.arrival())) best = lvl;
            }
            return best < 0 ? null : q[best].pollFirst();
        }
        public long comparisons() { return cmp; }
    }

    static Triage make(boolean redesigned) { return redesigned ? new LevelQueuesTriage() : new RebuildTriage(); }

    public static void main(String[] args) {
        // ---- 1. Validation: both designs must serve the SAME patients in the SAME order
        boolean allEqual = true; int checks = 0;
        for (int seed = 0; seed < 30; seed++) {
            Random r = new Random(seed); Triage a = make(false), b = make(true); long id = 0;
            for (long tick = 0; tick < 1500; tick++) {
                int arrivals = r.nextInt(3);
                for (int k = 0; k < arrivals; k++) { Patient p = new Patient("P" + id, 1 + r.nextInt(5), tick); id++; a.admit(p); b.admit(p); }
                Patient x = a.serve(tick), y = b.serve(tick); checks++;
                if (!Objects.equals(x, y)) allEqual = false;
            }
        }
        System.out.println("Validation: " + checks + " serve decisions over 30 random simulations -> identical sequences: " + allEqual);

        // ---- 2. Same test conditions for both: n patients already waiting, then 200 serves (time keeps moving)
        int serves = 200; StringBuilder csv = new StringBuilder("n,design,ms_per_200_serves,us_per_serve,comparisons_per_serve,alloc_MB\n");
        System.out.println("\nn patients waiting, then " + serves + " serves (avg of 3 runs after warm-up)");
        System.out.printf("%-9s %-20s %14s %13s %18s %12s%n", "n", "design", "total ms", "us/serve", "comparisons/serve", "alloc MB");
        for (int n : new int[]{2_000, 10_000, 50_000}) {
            Random r = new Random(11); Patient[] ps = new Patient[n];
            for (int i = 0; i < n; i++) ps[i] = new Patient("P" + i, 1 + r.nextInt(5), i);
            for (boolean redesigned : new boolean[]{false, true}) {
                double ms = 0, mb = 0; long cmp = 0;
                for (int run = 0; run < 4; run++) {
                    Triage t = make(redesigned); for (Patient p : ps) t.admit(p);
                    long a0 = MX.getThreadAllocatedBytes(Thread.currentThread().getId()); long t0 = System.nanoTime();
                    for (int k = 0; k < serves; k++) sink += t.serve(n + k).arrival();
                    double e = (System.nanoTime() - t0) / 1e6; double m = (MX.getThreadAllocatedBytes(Thread.currentThread().getId()) - a0) / 1048576.0;
                    if (run > 0) { ms += e; mb += m; cmp = t.comparisons(); }
                }
                ms /= 3; mb /= 3;
                String name = redesigned ? "5 level queues" : "rebuild PQ per serve";
                System.out.printf("%-9d %-20s %14.2f %13.1f %18d %12.2f%n", n, name, ms, ms * 1000 / serves, cmp / serves, mb);
                csv.append(String.format("%d,%s,%.3f,%.2f,%d,%.2f%n", n, name, ms, ms * 1000 / serves, cmp / serves, mb));
            }
        }
        try { java.nio.file.Files.writeString(java.nio.file.Path.of("raw_data.csv"), csv.toString()); } catch (Exception e) { }
        System.out.println("\n(sink=" + sink + ")");
    }
}
