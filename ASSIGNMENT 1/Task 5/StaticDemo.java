public class StaticDemo {
    public static void main(String[] args) {
        Facility north = new Facility("North"), south = new Facility("South");
        System.out.println(north.name + " ticket " + north.issueTicket() + " (instance counter " + north.localCounter + ")");
        System.out.println(south.name + " ticket " + south.issueTicket() + " (instance counter " + south.localCounter + ")  <- instance counter would repeat 1");
        System.out.println(north.name + " ticket " + north.issueTicket());
        System.out.println(Facility.summary());
        Facility.leave();
        System.out.println(Facility.summary());
        Object ticket = new Object();   // ticket released: set reference to null -> eligible for GC
        ticket = null;
        System.out.println("Ticket reference cleared -> object eligible for garbage collection");
    }
}

/** Task 5 - static counter, static block, static method, two facilities, object life cycle. */
class Facility {
    static int ticketCounter;          // shared across ALL facilities -> unique tickets
    static int totalOccupancy;
    static final String UNIVERSITY;
    static { UNIVERSITY = "SIMATS"; ticketCounter = 0; }   // static block runs once at class load
    int localCounter;                  // WRONG choice: per-facility numbers repeat

    final String name;
    Facility(String name) { this.name = name; }
    int issueTicket() { localCounter++; totalOccupancy++; return ++ticketCounter; }
    static void leave() { totalOccupancy--; }
    static String summary() { return UNIVERSITY + " occupancy = " + totalOccupancy; }
}
