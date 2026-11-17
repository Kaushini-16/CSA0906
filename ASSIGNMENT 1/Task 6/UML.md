# Task 6 - UML class diagram (Mermaid; renders on GitHub)

```mermaid
classDiagram
  class Chargeable { <<interface>> +chargingFee(int kWh) double }
  class Vehicle { <<abstract>> -String regNo +getRegNo() String +ratePerHour()* double +calculateFee(int minutes) double }
  class TwoWheeler { +ratePerHour() double }
  class Car { +ratePerHour() double }
  class ElectricCar { +chargingFee(int kWh) double }
  Vehicle <|-- TwoWheeler
  Vehicle <|-- Car
  Car <|-- ElectricCar
  Chargeable <|.. ElectricCar
```

Inheritance type: hierarchical (Vehicle -> TwoWheeler, Car) + multilevel (Vehicle -> Car -> ElectricCar).
Constructor order: Vehicle() -> Car() -> ElectricCar().
