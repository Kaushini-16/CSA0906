import java.util.*;

/** Task 1 - Alternative concept: parallel String arrays vs. Vehicle objects. */
public class ParallelVsObjects {

    static class VehicleRecord {
        String regNo; String type; long entryTime;
        VehicleRecord(String r, String t, long e) { regNo = r; type = t; entryTime = e; }
    }

    public static void main(String[] args) {
        int n = 200_000;
        // Design A: parallel arrays
        String[] regNo = new String[n]; String[] type = new String[n]; long[] entry = new long[n];
        // Design B: objects
        VehicleRecord[] objs = new VehicleRecord[n];
        for (int i = 0; i < n; i++) {
            regNo[i] = "TN" + i; type[i] = (i % 3 == 0) ? "BIKE" : "CAR"; entry[i] = i;
            objs[i] = new VehicleRecord(regNo[i], type[i], i);
        }
        String key = "TN" + (n - 1); // worst case: last element

        long t0 = System.nanoTime();
        int idxA = -1;
        for (int i = 0; i < n; i++) if (regNo[i].equals(key)) { idxA = i; break; }
        long tA = System.nanoTime() - t0;

        t0 = System.nanoTime();
        VehicleRecord found = null;
        for (VehicleRecord v : objs) if (v.regNo.equals(key)) { found = v; break; }
        long tB = System.nanoTime() - t0;

        System.out.println("Search worst case, n = " + n + " (both O(n))");
        System.out.println("Parallel arrays : index " + idxA + ", " + tA / 1000 + " us");
        System.out.println("Objects         : " + found.regNo + ", " + tB / 1000 + " us");
        System.out.println("Adding a new attribute: parallel arrays -> new array + edit every method;");
        System.out.println("objects -> add one field to VehicleRecord only.");
    }
}
