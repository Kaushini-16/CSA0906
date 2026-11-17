package parking.service;

import parking.model.Vehicle;

public class ParkingService {
    private final String[][] slots;            // private: only the service touches slots
    int allocations;                           // default (package-private) access: same package only

    public ParkingService(int[] slotsPerFloor) {
        slots = new String[slotsPerFloor.length][];
        for (int f = 0; f < slots.length; f++) slots[f] = new String[slotsPerFloor[f]];
    }
    public int[] park(Vehicle v) {
        for (int f = 0; f < slots.length; f++)
            for (int s = 0; s < slots[f].length; s++)
                if (slots[f][s] == null) { slots[f][s] = v.getRegNo(); allocations++; return new int[]{f, s}; }
        return null;
    }
    public boolean unpark(int floor, int slot) {
        if (floor < 0 || floor >= slots.length || slot < 0 || slot >= slots[floor].length || slots[floor][slot] == null) return false;
        slots[floor][slot] = null; return true;
    }
}
