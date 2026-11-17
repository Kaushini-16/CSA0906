# Q1 - Notes (prediction, evidence, decision)

## Prediction (written BEFORE running StringBenchmark)
- `String +` in a loop creates a new String (and copies the whole text so far) for every field: n fields -> n new Strings, about n^2/2 x (average field length) chars copied = **O(n^2)**. Strings are immutable, so nothing can be appended in place.
- `StringBuilder`/`StringBuffer` keep one growing char buffer: **O(n)** copies, one final String. `StringBuffer` is the same but every method is `synchronized`, so it should be a little slower.
- So: a short ticket -> difference negligible; 50 fields -> `+` clearly slower; 2,000 fields -> `+` dramatically slower.

## Workloads
A: 8 fields x 100,000 tickets | B: 50 fields x 10,000 tickets | C (mine): 2,000 fields x 50 tickets (one very long ticket, e.g. group booking).

## Observed (`output.txt`, `raw_data.csv`; 5 averaged runs after warm-up; allocation measured with ThreadMXBean)
| Workload | String + | StringBuilder | StringBuffer | allocated MB (+ / SB) |
|---|---|---|---|---|
| A | 17.7 ms | 27.8 ms | 23.2 ms | 71 / 47 |
| B | 11.6 ms | 4.9 ms | 7.6 ms | 172 / 30 |
| C | 94.0 ms | 0.8 ms | 0.9 ms | 1344 / 4.9 |

- **Prediction partly wrong:** on workload A `+` was *not* slower (the JIT/indy string-concat handles 8 small fields well and a StringBuilder has start-up and resize costs). With 8 fields the difference is negligible/noise.
- On B and C the O(n^2) behaviour appears: C is about 100x slower and allocates about 270x more memory with `+`.
- `String.join` / `StringJoiner` were as fast as or faster than a hand-written StringBuilder; `String.format` was the slowest (parses the format every call).
- Note: timings vary between machines and runs; the *growth pattern* is the evidence.

## Design decision
- **Per-ticket text:** `StringBuilder` (or `String.join`) - the builder is a local variable, never shared, so synchronisation is wasted cost.
- **Shared booking log:** `StringBuffer`, or better a synchronised/queue-based logger. `ThreadSafetyDemo` (`output_threads.txt`) shows 16 threads appending to a shared `StringBuilder` lost characters and threw `ArrayIndexOutOfBoundsException` in 10 of 10 trials, while `StringBuffer` always reached the expected length. (Caveat: `StringBuffer` makes each *single call* atomic, not a sequence of calls; a log line made of several appends can still interleave - build the whole line in a local StringBuilder and append once.)

## Alternative concept (join / StringJoiner / format)
`String.join("|", fields)` computes the total length first and fills one array: O(n), one allocation. `StringJoiner` is O(n) with a small wrapper. `String.format` is O(n) but with a large constant (about 10x slower here). Repeated `+`: copies 1+2+...+n field-lengths = O(n^2) chars, n temporary Strings. StringBuilder: O(n) amortised (capacity doubles), extra space O(n) for the buffer, 1 temporary String.
