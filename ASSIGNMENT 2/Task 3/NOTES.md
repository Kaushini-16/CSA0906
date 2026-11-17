# Q3 - Notes (hashCode-equals contract)

## Complete the Code
- Before TODO 1 (`AttendanceBefore.java`, `output_before.txt`): **Unique students: 3** - Arun is counted twice.
- After TODO 1 (`after/Attendance.java`, `output_after.txt`): **Unique students: 2** (expected output).
- Why: `HashSet.add` first computes `hashCode()` to choose a bucket, and only calls `equals()` on elements *in that same bucket*. Without an override, `Object.hashCode()` is identity-based, so two equal-looking Student objects almost always land in different buckets and `equals()` is never consulted. The contract: **if a.equals(b) then a.hashCode() == b.hashCode()** (the reverse need not hold). Fix: `hashCode()` must use the same field(s) as `equals()` -> `regNo.hashCode()`.

## Designed tests (predictions printed before each result in `output_tests.txt`)
| Test | Scenario | Predicted | Observed |
|---|---|---|---|
| 1 | The SAME Student object scanned twice, no hashCode -> set "appears to work" | 2 | 2 |
| 2 | Two equal but distinct objects (real scan scenario), no hashCode | 3 | 3 (duplicate) |
| 2b | Same scans with hashCode added | 2 | 2 |
| 3 | `regNo` changed after insertion (hashCode depends on it) | contains() false, size 2 after re-add | contains = false, size 2 |
| 4 | TreeSet with `compareTo` by name; two different students called Arun | 1 | 1 (second student silently lost) |

Test 1 is why the bug survives casual testing: same reference -> same identity hash. Test 3: the element sits in the old bucket, but lookups compute the new hash and look in a different bucket, so the element is "lost" (cannot be found or removed, a memory leak). Rule: **keys in hash collections should be immutable** (hence `private final regNo`). Test 4: TreeSet uses `compareTo == 0` as equality, so comparator/compareTo must be consistent with equals.

## Verifying the fix
Test 2b plus the `Attendance` output; also check the contract directly: equal objects -> equal hash codes, and run the scan list through the set in different orders.

## Alternative concept (timing, 1,000,000 scans, 100,000 unique students, 5 runs averaged)
HashSet 147 ms, HashMap<String,Student> 139 ms, TreeSet 1,021 ms. HashSet/HashMap: O(1) average per scan, O(n) space (HashMap keeps key + value + entry node, HashSet is built on a HashMap). TreeSet/comparator: O(log n) per scan (about 17 comparisons for 100,000), sorted iteration for free, O(n) space (tree nodes with 3 references). Note: the TreeSet timing used String keys while HashSet used Student objects, so the gap is, if anything, understated.
