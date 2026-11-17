import java.util.*;

/** Task 9 - abstract class + interfaces (Chargeable, Payable with default and static methods). */
public class AbstractionDemo {
    public static void main(String[] args) {
        List<Vehicle> list = List.of(new Car("TN02B2"), new ElectricCar("TN03C3"));
        for (Vehicle v : list) {
            double fee = v.calculateFee(130);
            if (v instanceof Chargeable c) fee += c.chargingFee(10);
            System.out.println(v.getRegNo() + " Rs " + fee + " | " + Payable.describe(v));
        }
        Payable student = new Student("S100", true);
        Payable visitor = new Visitor("V200");
        System.out.println("Student pays " + student.payable(90) + ", visitor pays " + visitor.payable(90));
    }
}

interface Chargeable { double chargingFee(int kWh); }

interface Payable {
    double payable(double fee);
    default boolean isFree() { return payable(100) == 0; }               // default method
    static String describe(Vehicle v) { return "payer-ready " + v.getRegNo(); } // static method
}
abstract class Vehicle {
    private final String regNo;
    protected Vehicle(String r) { regNo = r; }
    public String getRegNo() { return regNo; }
    protected abstract double ratePerHour();
    public double calculateFee(int m) { return ((m + 59) / 60) * ratePerHour(); }
}
class Car extends Vehicle {
    Car(String r) { super(r); }
    @Override protected double ratePerHour() { return 30; }
}
class ElectricCar extends Car implements Chargeable {
    ElectricCar(String r) { super(r); }
    @Override public double chargingFee(int kWh) { return kWh * 12; }
}
class Student implements Payable {
    final String id; final boolean hasPass;
    Student(String id, boolean pass) { this.id = id; hasPass = pass; }
    @Override public double payable(double fee) { return hasPass ? 0 : fee; }
}
class Visitor implements Payable {
    final String id;
    Visitor(String id) { this.id = id; }
    @Override public double payable(double fee) { return fee; }
}
