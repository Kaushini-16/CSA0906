# Task 1 output – A1 File Format Design

## Delimited text record
`passId,regNo,name,routeNo,boardingStop,validityDate,amountPaid`

Example:
`P1001,192311001,Arun,12,Main Gate,2027-03-31,3800.00`

Names containing commas, quotes, or line breaks must be escaped/quoted using a defined CSV format. Do not parse general CSV using a plain comma split.

## Binary record (`DataOutputStream`)
Write fields in a fixed documented order:
1. `writeUTF(passId)`
2. `writeUTF(regNo)`
3. `writeUTF(name)`
4. `writeInt(routeNo)`
5. `writeUTF(boardingStop)`
6. `writeUTF(validityDate)`
7. `writeDouble(amountPaid)`

The reader must read fields in exactly the same order.
