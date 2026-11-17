package parking.app;

import parking.model.Car;
import parking.service.ParkingService;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        ParkingService svc = new ParkingService(new int[]{2, 1});
        Car car = new Car("TN02B2");
        System.out.println("parked at " + Arrays.toString(svc.park(car)));
        car.setMinutes(130);
        System.out.println("fee = " + car.calculateFee());
        try { car.setMinutes(-5); } catch (IllegalArgumentException e) { System.out.println("rejected: " + e.getMessage()); }
        System.out.println("invalid slot release = " + svc.unpark(9, 9));
        // Wrapper / autoboxing pitfall
        Integer a = 1000, b = 1000, c = 100, d = 100;
        System.out.println("Integer 1000 == 1000 : " + (a == b) + "  equals: " + a.equals(b));
        System.out.println("Integer 100  == 100  : " + (c == d) + "  (cached -128..127)");
    }
}
