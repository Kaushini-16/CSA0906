public class TicketDemo {
    public static void main(String[] args) {
        ParkingTicket a = new ParkingTicket();
        ParkingTicket b = new ParkingTicket("TN01AB1111", 0, 1);
        ParkingTicket c = new ParkingTicket("TN02CD2222", 1, 0, 540);
        ParkingTicket d = new ParkingTicket(c);          // duplicate re-issue
        ParkingTicket e = ParkingTicket.of("TN03EF3333", 2, 4);
        for (ParkingTicket t : new ParkingTicket[]{a, b, c, d, e}) System.out.println(t);
        System.out.println("c == d ? " + (c == d) + " (different objects on heap, same ticket number)");
        // Time to create n tickets
        int n = 1_000_000; long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) new ParkingTicket("R" + i, 0, 0);
        System.out.println(n + " tickets created in " + (System.nanoTime() - t0) / 1_000_000 + " ms  -> O(n) time, O(n) space");
    }
}

/** Task 4 - Constructors, this, chaining, copy constructor. */
class ParkingTicket {
    private static int counter = 1000;
    private final int ticketNo;
    private final String regNo;
    private final int floor, slot;
    private final long entryMinute;

    ParkingTicket() { this("UNKNOWN", 0, 0); }                         // default
    ParkingTicket(String regNo, int floor, int slot) { this(regNo, floor, slot, 0); } // chained
    ParkingTicket(String regNo, int floor, int slot, long entryMinute) {   // full
        this.ticketNo = ++counter;
        this.regNo = regNo; this.floor = floor; this.slot = slot; this.entryMinute = entryMinute;
    }
    ParkingTicket(ParkingTicket t) {                                      // copy: duplicate ticket, SAME number
        this.ticketNo = t.ticketNo; this.regNo = t.regNo;
        this.floor = t.floor; this.slot = t.slot; this.entryMinute = t.entryMinute;
    }
    static ParkingTicket of(String regNo, int floor, int slot) { return new ParkingTicket(regNo, floor, slot, 0); } // factory
    @Override public String toString() { return "Ticket#" + ticketNo + " " + regNo + " [" + floor + "," + slot + "] @" + entryMinute; }
}
