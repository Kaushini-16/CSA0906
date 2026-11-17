package parking.model;

public final class VehicleFactory {
    private VehicleFactory() {}
    public static Vehicle create(String type, String regNo) {
        return switch (type.toUpperCase()) {
            case "BIKE" -> new TwoWheeler(regNo);
            case "CAR" -> new Car(regNo);
            case "EV" -> new ElectricCar(regNo);
            default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
        };
    }
}
