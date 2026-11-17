# Task 8 - Dynamic dispatch trace
`Vehicle v = new ElectricCar("TN03C3"); v.calculateFee(130);`
- Declared (compile-time) type: Vehicle -> compiler checks calculateFee(int) exists.
- Actual (runtime) type: ElectricCar -> JVM looks up the method in ElectricCar, then Car, then Vehicle.
- Method executed: Vehicle.calculateFee(int), which calls this.ratePerHour() -> Car.ratePerHour() = 30. Fee = 3 x 30 = 90.
- Compile-time polymorphism: calculateFee(int) vs calculateFee(LocalTime, LocalTime) chosen by argument types.

Last line prints **true** because equals() compares only regNo (instanceof Vehicle, not same class). This is correct: one physical vehicle has one registration number, whatever object type represents it.
