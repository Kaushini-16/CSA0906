# Task 1 - Problem Analysis (summary for the report)

**Inputs:** vehicle registration no., vehicle type, user category (student/staff/visitor), pass status, entry time, exit time, kWh used (EV).
**Outputs:** allocated {floor, slot}, parking ticket (unique number), fee on exit, "facility full" / error messages, daily collection.

| Noun -> Class | Why it is needed | Responsibility |
|---|---|---|
| Vehicle (TwoWheeler, Car, ElectricCar) | Type decides the rate | Hold regNo, give hourly rate |
| ParkingSlot / ParkingFacility | Slots are limited and shared | Store floors x slots, allocate, release |
| ParkingTicket | Links vehicle, slot, time | Unique number, entry time, exit time |
| User / Pass | Pass holders park free | Say whether fee is waived |
| FeeCalculator | Rules change often | Compute fee from type + duration |

| Verb -> Method | Class |
|---|---|
| enter / allocate | ParkingFacility |
| leave / release | ParkingFacility |
| issue ticket | ParkingTicket (constructor) |
| compute fee | Vehicle / FeeCalculator |

**Business rules:** a slot holds at most one vehicle; a ticket number is never reused; a vehicle cannot be parked twice; exit time >= entry time; fee >= 0; pass holders pay 0; charging only for EVs.

**Assumptions:** time in whole minutes; every started hour is charged; EV charging is billed separately per kWh; floors may have different slot counts.

**Complexity (alternative):** search by regNo is O(n) in both designs. Parallel arrays use 3 separate arrays (no object header, ~12-16 bytes saved per vehicle) but adding an attribute means a new array and edits in every method; the object design changes one class.
