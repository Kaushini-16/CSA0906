# Task 3 - Pseudocode

```
ALGORITHM AllocateSlot(slots, vehicleNo)
  INPUT : jagged array slots[F][], vehicleNo
  OUTPUT: {floor, slot} or NULL
  FOR f <- 0 TO F-1 DO
    FOR s <- 0 TO length(slots[f])-1 DO
      IF slots[f][s] = NULL THEN
        slots[f][s] <- vehicleNo
        RETURN {f, s}              // stop at first free slot
      END IF
    END FOR
  END FOR
  RETURN NULL                      // facility full
END ALGORITHM

ALGORITHM ComputeFee(minutes, rate, hasPass)
  IF hasPass THEN RETURN 0
  IF minutes <= 0 THEN RETURN 0
  hours <- (minutes + 59) DIV 60
  IF hours <= 1 THEN fee <- rate
  ELSE fee <- rate + (hours - 1) * rate * 0.5      // additional hours at half rate
  END IF
  IF fee > 8 * rate THEN fee <- 8 * rate           // daily maximum
  RETURN fee
END ALGORITHM
```

Complexity: allocate O(F*S) time, O(1) extra space. Alternative (free count + stack): allocate O(F), release O(1), extra space O(F*S).
