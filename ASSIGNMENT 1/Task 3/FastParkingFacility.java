import java.util.*;

/** Alternative concept: free-slot count per floor + stack of free slots. allocate O(F), release O(1). */
public class FastParkingFacility {
    private final String[][] slots;
    private final int[] freeCount;
    private final Deque<Integer>[] freeStack;

    @SuppressWarnings("unchecked")
    public FastParkingFacility(int[] slotsPerFloor) {
        slots = new String[slotsPerFloor.length][];
        freeCount = new int[slotsPerFloor.length];
        freeStack = new Deque[slotsPerFloor.length];
        for (int f = 0; f < slotsPerFloor.length; f++) {
            slots[f] = new String[slotsPerFloor[f]];
            freeCount[f] = slotsPerFloor[f];
            freeStack[f] = new ArrayDeque<>();
            for (int s = slotsPerFloor[f] - 1; s >= 0; s--) freeStack[f].push(s);
        }
    }
    public int[] allocate(String v) {
        for (int f = 0; f < slots.length; f++) {      // O(F) - skip full floors
            if (freeCount[f] > 0) {
                int s = freeStack[f].pop();           // O(1)
                slots[f][s] = v; freeCount[f]--;
                return new int[]{f, s};
            }
        }
        return null;
    }
    public boolean release(int f, int s) {
        if (f < 0 || f >= slots.length || s < 0 || s >= slots[f].length || slots[f][s] == null) return false;
        slots[f][s] = null; freeCount[f]++; freeStack[f].push(s);   // O(1)
        return true;
    }
    public static void main(String[] a) {
        FastParkingFacility p = new FastParkingFacility(new int[]{2, 1});
        System.out.println(Arrays.toString(p.allocate("A")) + Arrays.toString(p.allocate("B")) + Arrays.toString(p.allocate("C")) + p.allocate("D"));
        System.out.println(p.release(0, 1) + " " + p.release(0, 1) + " " + p.release(5, 0));
        System.out.println(Arrays.toString(p.allocate("D")));
    }
}
