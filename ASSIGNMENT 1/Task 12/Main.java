package parking.app;

import parking.model.*;
import parking.service.ParkingLot;
import java.util.*;

public class Main {
    static int pass, fail;
    static void check(String name, Object expected, Object actual) {
        boolean ok = Objects.equals(expected, actual);
        if (ok) pass++; else fail++;
        System.out.printf("| %-48s | %-10s | %-10s | %s |%n", name, expected, actual, ok ? "PASS" : "FAIL");
    }
    static double run(String type, String reg, UserCategory c, Pass p, int in, int out, int kWh) {
        ParkingLot l = new ParkingLot(new int[]{1});
        l.enter(VehicleFactory.create(type, reg), c, p, in);
        return l.exit(reg, out, kWh);
    }

    public static void main(String[] args) {
        System.out.println("| Test case                                        | Expected   | Actual     | Result |");
        System.out.println("|--------------------------------------------------|------------|------------|--------|");
        check("E-bike 10 min -> flat 15", 15.0, run("EBIKE", "EB1", UserCategory.VISITOR, null, 480, 490, 0));
        check("E-bike 300 min -> still flat 15", 15.0, run("EBIKE", "EB2", UserCategory.VISITOR, null, 480, 780, 0));
        check("Van 9:00-11:00 (peak) 2 x 60", 120.0, run("VAN", "V1", UserCategory.VISITOR, null, 540, 660, 0));
        check("Van 8:00-10:00 (1 off-peak + 1 peak)", 90.0, run("VAN", "V2", UserCategory.VISITOR, null, 480, 600, 0));
        check("Van 17:00-18:00 (off-peak) 30", 30.0, run("VAN", "V3", UserCategory.VISITOR, null, 1020, 1080, 0));
        check("Visitor car, one-day pass, 600 min: fee only", 0.0, run("CAR", "C1", UserCategory.VISITOR, new OneDayPass(), 480, 1080, 0));
        check("Visitor car, one-day pass across midnight", 240.0, run("CAR", "C2", UserCategory.VISITOR, new OneDayPass(), 1300, 1900, 0));
        check("Student monthly pass free", 0.0, run("CAR", "C3", UserCategory.STUDENT, new MonthlyPass(), 480, 600, 0));
        check("Old rule unchanged: car 130 min", 90.0, run("CAR", "C4", UserCategory.VISITOR, null, 480, 610, 0));
        check("Old rule unchanged: EV 130 min + 10 kWh", 210.0, run("EV", "E1", UserCategory.VISITOR, null, 480, 610, 10));

        // 5 floors with different slot counts: 20+30+40+50+60 = 200
        ParkingLot big = new ParkingLot(new int[]{20, 30, 40, 50, 60});
        int parked = 0;
        try { for (int i = 0; i < 300; i++) { big.enter(new Car("X" + i), UserCategory.VISITOR, null, 0); parked++; } }
        catch (IllegalStateException e) { /* full */ }
        check("5-floor jagged facility capacity", 200, parked);

        // Day scenario, hand calculated = 305
        ParkingLot day = new ParkingLot(new int[]{20, 30, 40, 50, 60});
        day.enter(VehicleFactory.create("VAN", "D1"), UserCategory.VISITOR, null, 480);
        day.enter(VehicleFactory.create("EBIKE", "D2"), UserCategory.VISITOR, null, 480);
        day.enter(VehicleFactory.create("CAR", "D3"), UserCategory.VISITOR, new OneDayPass(), 480);
        day.enter(VehicleFactory.create("EV", "D4"), UserCategory.VISITOR, null, 480);
        day.exit("D1", 600, 0);   // 30 + 60 = 90
        day.exit("D2", 580, 0);   // 15
        day.exit("D3", 1080, 0);  // pass 50 collected at entry, fee 0
        day.exit("D4", 540, 10);  // 30 + 120 = 150
        check("Day collection (hand calc = 305)", 305.0, day.getCollection());
        System.out.println("\nPassed " + pass + ", failed " + fail);
    }
}
