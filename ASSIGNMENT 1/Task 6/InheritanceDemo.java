/** Task 6 - Hierarchy: Vehicle <- Car <- ElectricCar, TwoWheeler. Constructor trace. */
public class InheritanceDemo {
    public static void main(String[] args) {
        System.out.println("Creating ElectricCar:");
        ElectricCar e = new ElectricCar("TN03C3", 40);
        System.out.println(e.describe());
    }
}

class Vehicle {
    private final String regNo;
    Vehicle(String regNo) { this.regNo = regNo; System.out.println("  Vehicle constructor: " + regNo); }
    String getRegNo() { return regNo; }
    String describe() { return "Vehicle " + regNo; }
}
class Car extends Vehicle {
    Car(String regNo) { super(regNo); System.out.println("  Car constructor"); }
    @Override String describe() { return "Car " + super.describe(); }
}
class ElectricCar extends Car {
    private final int batteryKWh;
    ElectricCar(String regNo, int batteryKWh) {
        super(regNo); this.batteryKWh = batteryKWh;
        System.out.println("  ElectricCar constructor, battery " + batteryKWh + " kWh");
    }
    @Override String describe() { return "Electric " + super.describe() + " (" + batteryKWh + " kWh)"; }
}
class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) { super(regNo); }
}
