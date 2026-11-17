# Q4 - Notes (pharmacy stock maps)

## Prediction (before running)
| | put | get | iterate | natural report |
|---|---|---|---|---|
| HashMap | O(1) avg | O(1) avg | O(capacity + n), no defined order | neither |
| TreeMap (red-black tree) | O(log n) | O(log n) | O(n) in key order | **code-order** report |
| LinkedHashMap | O(1) avg | O(1) avg | O(n) in insertion order | **insertion-order** report |
"Wrong" map extra work: HashMap/LinkedHashMap need `sort(keys)` for the code-order report = O(n log n) each evening; HashMap/TreeMap cannot give insertion order at all - a second list of codes must be maintained (O(n) space, discipline on every put).

## Evidence (`PharmacyMaps.java`, `output.txt`, `raw_data.csv`; n = 10k / 100k / 500k, 100,000 lookups, 5 averaged runs)
| n | HashMap lookups | TreeMap lookups | LinkedHashMap lookups | code-order report: TreeMap / HashMap (sorted) |
|---|---|---|---|---|
| 10,000 | 6.2 ms (62 ns each) | 85 ms (854 ns) | 13.8 ms | 1.1 ms / 7.6 ms |
| 100,000 | 11.1 ms | 90 ms | 5.5 ms | 6.7 ms / 91.7 ms |
| 500,000 | 7.9 ms (79 ns) | 192 ms (1,921 ns) | 9.6 ms | 41.7 ms / 294.9 ms |
- Order check (in the output table): TreeMap and LinkedHashMap produced exactly the required order for their own report; HashMap did not produce insertion order (NO), and needed the sort for code order.
- TreeMap lookup cost grows with n (about 17-19 String comparisons at 100k-500k); HashMap lookup stays flat (about 60-110 ns).
- The sorted array + `binarySearch` also worked for lookups but was 10x+ slower than HashMap in our run and build cost is a full sort.

## Acceptability criterion and decision
Acceptable response for one lookup: **under 1 ms** (a pharmacist cannot perceive less, and the screen/network dominate); every structure meets this by a factor of 500+ (worst: TreeMap 1.9 microseconds). The decision therefore rests on the *reports*: the order requirement is the tie-breaker.
**Deploy:** `HashMap` for lookups plus the evening code-order report built with one sort (hundreds of thousands of medicines sort in about 0.3 s once per day), **or** a `LinkedHashMap` (insertion-order report for free, lookups as fast as HashMap) with the code-order report sorted nightly. If the code-order report were needed on screen continuously, use `TreeMap` (O(log n) lookups, still microseconds). Remaining cost: for HashMap, hashing the String key and the cache miss to the bucket/entry; for TreeMap, the log n key comparisons and pointer chasing; for reports, the O(n log n) sort.
**Hashtable** is not preferred in new code: it is a legacy class whose every method is `synchronized` (needless lock cost for single-threaded code, and still not a sufficient lock for compound operations like check-then-put), it rejects null keys/values, has no defined order, and has been superseded by `HashMap` (single thread) or `ConcurrentHashMap` (multi thread).

## Alternative - sorted array + `Arrays.binarySearch`
Lookup O(log n); insertion O(n) (shift to keep order); code-order report O(n) (already sorted); build O(n log n); space O(n) with no per-entry node objects (smaller than TreeMap/HashMap). Good for a static, read-mostly catalogue; poor when medicines are added frequently (HashMap O(1), TreeMap O(log n) inserts).
