import java.util.*;

/** Q3 - three designed tests + alternatives. Predictions are printed BEFORE each result. */
public class ContractTests {
    /** Broken version: equals on regNo, NO hashCode. */
    static class BadStudent {
        final String regNo, name;
        BadStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof BadStudent b && regNo.equals(b.regNo); }
    }
    /** Corrected version, immutable key. */
    static final class GoodStudent {
        private final String regNo, name;
        GoodStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof GoodStudent g && regNo.equals(g.regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
        @Override public String toString() { return regNo + " " + name; }
    }
    /** Mutable key - hashCode depends on a field that can change. */
    static class MutableStudent {
        String regNo; final String name;
        MutableStudent(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof MutableStudent m && regNo.equals(m.regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
    }
    /** compareTo inconsistent with equals: compares by name only. */
    static class NameOrdered implements Comparable<NameOrdered> {
        final String regNo, name;
        NameOrdered(String r, String n) { regNo = r; name = n; }
        @Override public boolean equals(Object o) { return o instanceof NameOrdered s && regNo.equals(s.regNo); }
        @Override public int hashCode() { return regNo.hashCode(); }
        @Override public int compareTo(NameOrdered o) { return name.compareTo(o.name); }
    }

    public static void main(String[] args) {
        // Test 1: looks correct - the SAME object is scanned twice (identity hash codes are equal)
        BadStudent arun = new BadStudent("192311001", "Arun");
        Set<BadStudent> t1 = new HashSet<>();
        t1.add(arun); t1.add(new BadStudent("192311002", "Divya")); t1.add(arun);
        System.out.println("Test 1 (same object scanned twice, no hashCode)");
        System.out.println("  Prediction: 2 (same reference -> same identity hash, equals true)   Observed: " + t1.size());

        // Test 2: two DIFFERENT objects for the same student
        Set<BadStudent> t2 = new HashSet<>();
        t2.add(new BadStudent("192311001", "Arun")); t2.add(new BadStudent("192311002", "Divya")); t2.add(new BadStudent("192311001", "Arun"));
        System.out.println("Test 2 (equal but distinct objects, no hashCode)");
        System.out.println("  Prediction: 3 normally (different identity hashes -> usually different buckets, equals never called)   Observed: " + t2.size());
        Set<GoodStudent> t2fixed = new HashSet<>();
        t2fixed.add(new GoodStudent("192311001", "Arun")); t2fixed.add(new GoodStudent("192311002", "Divya")); t2fixed.add(new GoodStudent("192311001", "Arun"));
        System.out.println("Test 2b (same scans, hashCode added)  Prediction: 2   Observed: " + t2fixed.size());

        // Test 3: field changed after insertion
        MutableStudent m = new MutableStudent("192311001", "Arun");
        Set<MutableStudent> t3 = new HashSet<>();
        t3.add(m);
        m.regNo = "192311999";                              // changes hashCode after insertion
        System.out.println("Test 3 (regNo changed after insertion)");
        System.out.println("  Prediction: size 1 but contains(m) = false, and adding the same student again gives size 2");
        boolean contains = t3.contains(m);
        t3.add(m);
        System.out.println("  Observed: contains(m) = " + contains + ", size after adding again = " + t3.size());

        // Test 4: TreeSet whose compareTo is inconsistent with equals
        Set<NameOrdered> t4 = new TreeSet<>();
        t4.add(new NameOrdered("192311001", "Arun")); t4.add(new NameOrdered("192311050", "Arun"));   // different students, same name
        System.out.println("Test 4 (TreeSet, compareTo by name; two DIFFERENT students named Arun)");
        System.out.println("  Prediction: 1 (compareTo == 0 treated as duplicate although equals is false)   Observed: " + t4.size());

        // Alternatives: HashMap<String,Student> and TreeSet with comparator
        Map<String, GoodStudent> map = new HashMap<>();
        for (String[] s : new String[][]{{"192311001", "Arun"}, {"192311002", "Divya"}, {"192311001", "Arun"}}) map.putIfAbsent(s[0], new GoodStudent(s[0], s[1]));
        TreeSet<GoodStudent> tree = new TreeSet<>(Comparator.comparing(g -> g.toString().split(" ")[0]));
        tree.add(new GoodStudent("192311001", "Arun")); tree.add(new GoodStudent("192311002", "Divya")); tree.add(new GoodStudent("192311001", "Arun"));
        System.out.println("Alternatives -> HashMap<String,Student>: " + map.size() + ", TreeSet with regNo comparator: " + tree.size());

        // Timing of de-duplicating n scans (5 averaged runs, 100 distinct IDs repeated)
        System.out.println("\nTime to process 1,000,000 scans with 100,000 unique students (avg of 5 runs, ms):");
        int scans = 1_000_000, unique = 100_000; String[] ids = new String[scans]; Random r = new Random(1);
        for (int i = 0; i < scans; i++) ids[i] = "19231" + r.nextInt(unique);
        for (String kind : new String[]{"HashSet", "TreeSet", "HashMap"}) {
            double tot = 0; int size = 0;
            for (int run = 0; run < 6; run++) {
                long t0 = System.nanoTime();
                if (kind.equals("HashSet")) { Set<GoodStudent> s = new HashSet<>(); for (String id : ids) s.add(new GoodStudent(id, "x")); size = s.size(); }
                else if (kind.equals("TreeSet")) { TreeSet<String> s = new TreeSet<>(); for (String id : ids) s.add(id); size = s.size(); }
                else { Map<String, GoodStudent> s = new HashMap<>(); for (String id : ids) s.putIfAbsent(id, new GoodStudent(id, "x")); size = s.size(); }
                if (run > 0) tot += (System.nanoTime() - t0) / 1e6;
            }
            System.out.printf("  %-8s %8.1f ms   unique = %d%n", kind, tot / 5, size);
        }
    }
}
