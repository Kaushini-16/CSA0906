import java.util.*;

/** Q6 - own design: PriorityQueue triage, hand check, growth, starvation + aging, alternatives. */
public class TriageDesign {
    record Patient(String name, int severity, long arrival) {
        @Override public String toString() { return name + "(S" + severity + ")"; }
    }
    static final Comparator<Patient> RULE = Comparator.comparingInt(Patient::severity).thenComparingLong(Patient::arrival);
    static long sink;

    /** Aging rule: every WAIT_STEP time units waited improves effective severity by one level (never below 1). */
    static final int WAIT_STEP = 50;
    static int effective(Patient p, long now) { return Math.max(1, p.severity() - (int) ((now - p.arrival()) / WAIT_STEP)); }

    public static void main(String[] args) {
        // ---- Correctness check with 8 patients, answer worked out by hand
        String[] names = {"Ravi", "Meena", "John", "Asha", "Kumar", "Lata", "Divya", "Prem"};
        int[] sev = {3, 1, 3, 5, 1, 2, 2, 4};
        PriorityQueue<Patient> pq = new PriorityQueue<>(RULE);
        for (int i = 0; i < names.length; i++) pq.add(new Patient(names[i], sev[i], i));
        System.out.println("Hand-worked expected order : Meena(S1) Kumar(S1) Lata(S2) Divya(S2) Ravi(S3) John(S3) Prem(S4) Asha(S5)");
        System.out.println("Printing the PQ directly   : " + pq + "   <- heap array order, NOT treatment order");
        List<Patient> waiting = new ArrayList<>(pq); waiting.sort(RULE);          // display without disturbing the queue
        System.out.println("Waiting list (sorted copy) : " + waiting);
        StringBuilder out = new StringBuilder(); Patient p;
        while ((p = pq.poll()) != null) out.append(p).append(' ');
        String got = out.toString().trim();
        System.out.println("Actual poll() order        : " + got);
        System.out.println("Matches hand calculation   : " + got.equals("Meena(S1) Kumar(S1) Lata(S2) Divya(S2) Ravi(S3) John(S3) Prem(S4) Asha(S5)"));

        // ---- Growth: prediction add O(log n), poll O(log n)
        System.out.println("\nGrowth of PriorityQueue (avg of 3 runs, ns per operation):");
        System.out.printf("%-12s %14s %14s%n", "n", "add ns/op", "poll ns/op");
        for (int n : new int[]{10_000, 100_000, 1_000_000}) {
            double add = 0, poll = 0;
            for (int run = 0; run < 4; run++) {
                Random r = new Random(1); PriorityQueue<Patient> q = new PriorityQueue<>(RULE);
                long t0 = System.nanoTime();
                for (int i = 0; i < n; i++) q.add(new Patient("P", 1 + r.nextInt(5), i));
                long t1 = System.nanoTime();
                while (!q.isEmpty()) sink += q.poll().arrival();
                long t2 = System.nanoTime();
                if (run > 0) { add += (t1 - t0) / (double) n; poll += (t2 - t1) / (double) n; }
            }
            System.out.printf("%-12d %14.0f %14.0f%n", n, add / 3, poll / 3);
        }

        // ---- Starvation: 2 severe patients arrive per tick, 1 is served per tick
        System.out.println("\nStarvation experiment (2 arrivals of severity 1-2 per tick, 1 served per tick, 2,000 ticks):");
        PriorityQueue<Patient> plain = new PriorityQueue<>(RULE); Random r = new Random(9);
        plain.add(new Patient("Asha", 5, 0)); long id = 1; boolean served = false;
        for (long tick = 0; tick < 2000 && !served; tick++) {
            for (int k = 0; k < 2; k++) plain.add(new Patient("X" + id, 1 + r.nextInt(2), id++));
            served = plain.poll().name().equals("Asha");
        }
        System.out.println("  No aging  : Asha (severity 5) served? " + served + "   still waiting behind " + plain.size() + " patients");
        List<Patient> list = new ArrayList<>(); list.add(new Patient("Asha", 5, 0)); id = 1; r = new Random(9); long servedAt = -1;
        for (long tick = 0; tick < 2000 && servedAt < 0; tick++) {
            for (int k = 0; k < 2; k++) list.add(new Patient("X" + id, 1 + r.nextInt(2), id++));
            final long now = tick;
            PriorityQueue<Patient> aged = new PriorityQueue<>(Comparator.comparingInt((Patient x) -> effective(x, now)).thenComparingLong(Patient::arrival));
            aged.addAll(list);                                                  // rebuild with the current effective severities
            Patient next = aged.poll(); list.remove(next);
            if (next.name().equals("Asha")) servedAt = tick;
        }
        System.out.println("  With aging (1 level per " + WAIT_STEP + " time units): Asha served at tick " + servedAt + " (a severity-s patient reaches the top level after at most (s-1) x " + WAIT_STEP + " time units)");

        // ---- Alternatives: sorted LinkedList and five FIFO queues
        System.out.println("\nAlternatives, n admits then n serves (avg of 3 runs, total ms):");
        System.out.printf("%-10s %16s %16s %16s%n", "n", "PriorityQueue", "Sorted LinkedList", "5 FIFO queues");
        for (int n : new int[]{5_000, 20_000, 50_000}) {
            double a = 0, b = 0, c = 0;
            for (int run = 0; run < 4; run++) {
                int[] sevs = new int[n]; Random rr = new Random(2); for (int i = 0; i < n; i++) sevs[i] = 1 + rr.nextInt(5);
                long t0 = System.nanoTime(); PriorityQueue<Patient> q = new PriorityQueue<>(RULE);
                for (int i = 0; i < n; i++) q.add(new Patient("P", sevs[i], i)); while (!q.isEmpty()) sink += q.poll().arrival();
                double ta = (System.nanoTime() - t0) / 1e6;

                t0 = System.nanoTime(); LinkedList<Patient> ll = new LinkedList<>();
                for (int i = 0; i < n; i++) {                                  // insert at the correct position: O(n)
                    Patient np = new Patient("P", sevs[i], i); ListIterator<Patient> it = ll.listIterator();
                    while (it.hasNext()) { if (RULE.compare(it.next(), np) > 0) { it.previous(); break; } }
                    it.add(np);
                }
                while (!ll.isEmpty()) sink += ll.removeFirst().arrival();
                double tb = (System.nanoTime() - t0) / 1e6;

                t0 = System.nanoTime(); @SuppressWarnings("unchecked") ArrayDeque<Patient>[] five = new ArrayDeque[6];
                for (int s = 1; s <= 5; s++) five[s] = new ArrayDeque<>();
                for (int i = 0; i < n; i++) five[sevs[i]].addLast(new Patient("P", sevs[i], i));
                for (int served2 = 0; served2 < n; served2++) for (int s = 1; s <= 5; s++) if (!five[s].isEmpty()) { sink += five[s].pollFirst().arrival(); break; }
                double tc = (System.nanoTime() - t0) / 1e6;
                if (run > 0) { a += ta; b += tb; c += tc; }
            }
            System.out.printf("%-10d %16.1f %16.1f %16.1f%n", n, a / 3, b / 3, c / 3);
        }
        System.out.println("\n(sink=" + sink + ")");
    }
}
