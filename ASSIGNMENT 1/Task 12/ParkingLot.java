package parking.service;

import parking.model.*;
import java.util.*;

public class ParkingLot {
    private final String[][] slots;                                  // jagged array
    private final Map<String, ParkingTicket> active = new HashMap<>();
    private double collection;

    public ParkingLot(int[] slotsPerFloor) {
        slots = new String[slotsPerFloor.length][];
        for (int f = 0; f < slots.length; f++) slots[f] = new String[slotsPerFloor[f]];
    }
    public ParkingTicket enter(Vehicle v, UserCategory c, Pass pass, int entryMinute) {
        if (active.containsKey(v.getRegNo())) throw new IllegalStateException("Duplicate registration: " + v.getRegNo());
        for (int f = 0; f < slots.length; f++)
            for (int s = 0; s < slots[f].length; s++)
                if (slots[f][s] == null) {
                    slots[f][s] = v.getRegNo();
                    ParkingTicket t = new ParkingTicket(v, c, pass, f, s, entryMinute);
                    if (pass != null) collection += pass.price();
                    active.put(v.getRegNo(), t);
                    return t;
                }
        throw new IllegalStateException("Facility full");
    }
    /** Releases the slot, returns the fee (including EV charging). */
    public double exit(String regNo, int exitMinute, int kWh) {
        ParkingTicket t = active.get(regNo);
        if (t == null) throw new NoSuchElementException("No active ticket for " + regNo);
        if (exitMinute < t.getEntryMinute()) throw new IllegalArgumentException("exit before entry");
        double fee = 0;
        if (!t.isFeeWaived(exitMinute)) {
            fee = t.getVehicle().calculateFee(t.getEntryMinute(), exitMinute - t.getEntryMinute());
            if (t.getVehicle() instanceof Chargeable c) fee += c.chargingFee(kWh);
        }
        slots[t.getFloor()][t.getSlot()] = null;
        active.remove(regNo);
        collection += fee;
        return fee;
    }
    public boolean release(int floor, int slot) {
        if (floor < 0 || floor >= slots.length || slot < 0 || slot >= slots[floor].length || slots[floor][slot] == null) return false;
        String reg = slots[floor][slot];
        slots[floor][slot] = null; active.remove(reg); return true;
    }
    public double getCollection() { return collection; }
    public int activeCount() { return active.size(); }
}
