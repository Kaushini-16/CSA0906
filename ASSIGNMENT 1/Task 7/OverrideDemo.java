import java.util.*;

/** Task 7 - overriding, @Override, final, equals/hashCode/toString. */
public class OverrideDemo {
    public static void main(String[] args) {
        Vehicle a = new Car("TN02B2"), b = new ElectricCar("TN02B2"), c = new TwoWheeler("TN09Z9");
        System.out.println(a + " | " + c);
        System.out.println("a.equals(b) = " + a.equals(b) + "  hash equal = " + (a.hashCode() == b.hashCode()));
        Set<Vehicle> set = new HashSet<>(List.of(a, c));
        System.out.println("HashSet contains b (same regNo)? " + set.contains(b));
        for (Vehicle v : List.of(a, b, c)) System.out.println(v.getRegNo() + " fee for 130 min = " + v.calculateFee(130));
    }
}

abstract class Vehicle {
    private final String regNo;               // final variable
    protected Vehicle(String regNo) { this.regNo = regNo; }
    public final String getRegNo() { return regNo; }          // final method: cannot be overridden
    protected abstract double ratePerHour();
    public double calculateFee(int minutes) { return ((minutes + 59) / 60) * ratePerHour(); }
    @Override public boolean equals(Object o) { return o instanceof Vehicle v && regNo.equals(v.regNo); }
    @Override public int hashCode() { return regNo.hashCode(); }
    @Override public String toString() { return getClass().getSimpleName() + "(" + regNo + ")"; }
}
class TwoWheeler extends Vehicle {
    TwoWheeler(String r) { super(r); }
    @Override protected double ratePerHour() { return 10; }
}
class Car extends Vehicle {
    Car(String r) { super(r); }
    @Override protected double ratePerHour() { return 30; }
}
class ElectricCar extends Car {
    ElectricCar(String r) { super(r); }
    @Override public double calculateFee(int minutes) { return super.calculateFee(minutes) + 0; } // specialised hook
}
