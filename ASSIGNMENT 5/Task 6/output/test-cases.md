# Task 6 output – B1 test evidence

| Test | Input | Expected behaviour | Actual result |
|---|---|---|---|
| Add/find normal pass | P1003 | Record is returned | Fill after running |
| Missing ID | NOPE | `null` / NOT_FOUND | Fill after running |
| Damaged line | wrong field count | Skip malformed line and continue | Fill after running |
| Route listing | route 7 | Only valid route-7 records listed | Fill after running |

Benchmark CSV template is in `Task2/benchmark-results-template.csv`; add a separate search-growth CSV here with actual times.
