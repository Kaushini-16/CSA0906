import java.util.*;

/** Task 2 - Data types, jagged array, casting risk, Scanner input. */
public class DataRepresentation {

    // WRONG: integer division loses the started hour
    static int wrongHours(int minutes) { return minutes / 60; }
    // RIGHT: ceiling using integers only
    static int hours(int minutes) { return (minutes + 59) / 60; }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int[] slotsPerFloor = {3, 2, 4};          // jagged: different slots per floor
        String[][] slots = new String[slotsPerFloor.length][];
        for (int f = 0; f < slots.length; f++) slots[f] = new String[slotsPerFloor[f]];

        System.out.print("Enter parking minutes: ");
        int minutes = sc.hasNextInt() ? sc.nextInt() : 130;
        double rate = 30.0;

        double wrongFee = wrongHours(minutes) * rate;
        double wrongFee2 = (double) (minutes / 60) * rate;     // cast AFTER division: still wrong
        double doubleFee = Math.ceil(minutes / 60.0) * rate;   // floating point version
        double rightFee = hours(minutes) * rate;

        System.out.println("minutes = " + minutes);
        System.out.println("Integer division fee      : Rs " + wrongFee);
        System.out.println("Cast after division fee   : Rs " + wrongFee2);
        System.out.println("Math.ceil(min/60.0) fee   : Rs " + doubleFee);
        System.out.println("(m+59)/60 integer fee     : Rs " + rightFee);

        // 1D representation: index = floor * slotsPerFloor + slot (only valid when floors are equal)
        int perFloor = 50, floor = 2, slot = 7;
        System.out.println("1D index of floor 2 slot 7 (50 per floor) = " + (floor * perFloor + slot));
        long total = 0; for (String[] f : slots) total += f.length;
        System.out.println("Total slots in jagged array = " + total);
    }
}
