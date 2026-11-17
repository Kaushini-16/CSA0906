import java.util.*;

/** Q4 - HashMap vs TreeMap vs LinkedHashMap (+ Hashtable, sorted array + binarySearch) for pharmacy stock. */
public class PharmacyMaps {
    record Medicine(String code, String name, int stock) {}
    static long sink;
    static final int LOOKUPS = 100_000, RUNS = 5;

    static List<Medicine> data(int n) {
        List<Integer> nums = new ArrayList<>();
        for (int i = 0; i < n; i++) nums.add(i * 7 + 1000);
        Collections.shuffle(nums, new Random(7));                      // insertion order is NOT code order
        List<Medicine> out = new ArrayList<>();
        for (int x : nums) out.add(new Medicine("MED" + String.format("%08d", x), "Drug" + x, x % 500));
        return out;
    }

    public static void main(String[] args) {
        StringBuilder csv = new StringBuilder("n,structure,build_ms,lookup100k_ms,ns_per_lookup,codeOrderReport_ms,insertionOrderReport_ms,codeOrderOK,insertionOrderOK\n");
        for (int n : new int[]{10_000, 100_000, 500_000}) {
            List<Medicine> meds = data(n);
            String[] keys = new String[LOOKUPS]; Random rnd = new Random(3);
            for (int i = 0; i < LOOKUPS; i++) keys[i] = meds.get(rnd.nextInt(n)).code();
            List<String> insertion = new ArrayList<>(); for (Medicine m : meds) insertion.add(m.code());
            List<String> sortedCodes = new ArrayList<>(insertion); Collections.sort(sortedCodes);

            System.out.println("\n== n = " + n + " medicines, " + LOOKUPS + " lookups");
            System.out.printf("%-26s %9s %12s %10s %16s %18s %6s %6s%n", "Structure", "build ms", "100k lookups", "ns/lookup", "code-order rpt", "insertion-order rpt", "code?", "ins?");
            for (String kind : new String[]{"HashMap", "TreeMap", "LinkedHashMap", "Hashtable", "SortedArray+binarySearch"}) {
                double build = 0, look = 0, rep1 = 0, rep2 = 0; boolean ok1 = false, ok2 = false;
                for (int run = 0; run < RUNS + 1; run++) {
                    boolean warm = run == 0;
                    long t0 = System.nanoTime();
                    Map<String, Medicine> map = null; Medicine[] arr = null;
                    switch (kind) {
                        case "HashMap" -> map = new HashMap<>(); case "TreeMap" -> map = new TreeMap<>();
                        case "LinkedHashMap" -> map = new LinkedHashMap<>(); case "Hashtable" -> map = new Hashtable<>();
                        default -> { arr = meds.toArray(new Medicine[0]); Arrays.sort(arr, Comparator.comparing(Medicine::code)); }
                    }
                    if (map != null) for (Medicine m : meds) map.put(m.code(), m);
                    double b = (System.nanoTime() - t0) / 1e6;

                    t0 = System.nanoTime();
                    if (map != null) for (String k : keys) sink += map.get(k).stock();
                    else { String[] codes = new String[arr.length]; for (int i = 0; i < arr.length; i++) codes[i] = arr[i].code();
                           for (String k : keys) sink += arr[Arrays.binarySearch(codes, k)].stock(); }
                    double l = (System.nanoTime() - t0) / 1e6;

                    // Report 1: all medicines in CODE order. Natural iteration only valid for TreeMap / sorted array; others must sort.
                    t0 = System.nanoTime();
                    List<String> r1 = new ArrayList<>();
                    if (map instanceof TreeMap || arr != null) { if (map != null) r1.addAll(map.keySet()); else for (Medicine m : arr) r1.add(m.code()); }
                    else { r1.addAll(map.keySet()); Collections.sort(r1); }               // extra O(n log n) work
                    double e1 = (System.nanoTime() - t0) / 1e6;
                    // Report 2: in INSERTION order. Natural only for LinkedHashMap; others cannot reconstruct it (we show their natural order).
                    t0 = System.nanoTime();
                    List<String> r2 = new ArrayList<>();
                    if (map != null) r2.addAll(map.keySet()); else for (Medicine m : meds) r2.add(m.code());   // array: needs a separate insertion-order list
                    double e2 = (System.nanoTime() - t0) / 1e6;

                    if (!warm) { build += b; look += l; rep1 += e1; rep2 += e2; }
                    ok1 = r1.equals(sortedCodes); ok2 = r2.equals(insertion);
                }
                String ins = kind.startsWith("Sorted") ? "needs 2nd list" : String.format("%.2f", rep2 / RUNS);
                System.out.printf("%-26s %9.1f %12.2f %10.0f %16.2f %18s %6s %6s%n", kind, build / RUNS, look / RUNS, look / RUNS * 1e6 / LOOKUPS, rep1 / RUNS, ins, ok1 ? "yes" : "NO", kind.startsWith("Sorted") ? "n/a" : (ok2 ? "yes" : "NO"));
                csv.append(String.format("%d,%s,%.2f,%.3f,%.0f,%.2f,%.2f,%s,%s%n", n, kind, build / RUNS, look / RUNS, look / RUNS * 1e6 / LOOKUPS, rep1 / RUNS, rep2 / RUNS, ok1, ok2));
            }
        }
        try { java.nio.file.Files.writeString(java.nio.file.Path.of("raw_data.csv"), csv.toString()); } catch (Exception e) { }
        System.out.println("\n(sink=" + sink + ")");
    }
}
