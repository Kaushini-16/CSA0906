# Q6 - Notes (emergency triage)

## Design
- A plain FIFO `Queue` (LinkedList/ArrayDeque) serves strictly in arrival order, so a critical patient who arrives late would wait behind minor cases. We need order by (severity, then arrival).
- `PriorityQueue<Patient>` with `Comparator.comparingInt(severity).thenComparingLong(arrival)`. The `arrival` counter is a strictly increasing number given at `admit()`; two patients of equal severity differ in `arrival`, so the second key breaks every tie and the earlier arrival is served first. (A PriorityQueue by itself is **not stable** - without the counter, equal-severity patients can come out in any order.)
- `admit` = `add` (sift-up), `next` = `poll` (sift-down). Display the waiting list by sorting a *copy* (`new ArrayList<>(pq)` then `sort`), never by printing the queue.

## Complete the Code - `Triage.java`
Output `Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)` = expected (`output.txt`). Meena and Kumar are both S1; Meena was admitted earlier (`counter` 1 vs 4), so her tie-break key is smaller; likewise Ravi (0) before John (2).

## Correctness check (`design/TriageDesign.java`, `output_design.txt`)
Eight patients (Ravi3, Meena1, John3, Asha5, Kumar1, Lata2, Divya2, Prem4). By hand: severity 1 in arrival order -> Meena, Kumar; severity 2 -> Lata, Divya; 3 -> Ravi, John; 4 -> Prem; 5 -> Asha. The program's `poll()` order matched (true). **Printing the PQ directly** gave `[Meena, Kumar, Lata, Prem, Ravi, John, Divya, Asha]` - that is the internal binary-heap array: it only guarantees that each parent is <= its children (the head is the minimum); it is not sorted.

## Growth
Prediction: add O(log n), poll O(log n) (sift-down about 2 log n comparisons). Measured at n = 10k / 100k / 1M: add 230 / 107 / 102 ns, poll 513 / 345 / 411 ns per operation. Slow growth with n (the 10k figure includes JIT warm-up) is consistent with O(log n): 100x more patients cost only a few times more per operation. Alternatives (total ms for n admits + n serves; n = 5k/20k/50k): PriorityQueue 5.3 / 3.7 / 12.3; sorted LinkedList 64 / 760 / **18,390** (O(n) insert, grows about n^2); five FIFO queues 0.5 / 2.8 / 1.7 (O(1) admit, at most 5 checks to serve).

## Starvation and fair rule
A severity-5 patient is served only when no lower-numbered patient is waiting. If 2 severe patients arrive per tick and only 1 is served, the queue never empties: Asha (S5) was still waiting behind 2,001 patients after 2,000 ticks. **Aging rule:** effective severity = max(1, severity - waited / 50), ties by arrival. Every 50 time units of waiting raises a patient one level, so a severity-5 patient reaches level 1 within 200 time units; because Asha has the oldest arrival she wins the tie and was served at tick 193. (Cost: the priority now depends on time, so a plain PriorityQueue cannot hold it - see Q7.)

## Deployment decision
5,000 patients a day is about 3.5 per minute. A PriorityQueue handles about 10^7 operations per second here, and the waiting list is at most hundreds, so the basic design is far more than enough. Aging with the naive "rebuild per serve" is also fine at that size (microseconds); it becomes a bottleneck only for very long waiting lists (Q7). Yes - the design can serve a city hospital; the real constraints are clinical rules, persistence and concurrent access (use `PriorityBlockingQueue` or a lock if several desks admit patients), not the data structure.

## Alternative concept
- **Sorted LinkedList:** admit O(n) (walk to the position), serve O(1) (remove head); space O(n) with a 24+ byte node per patient. Measured 18 s at 50k vs 12 ms for the PQ.
- **Array of five FIFO queues:** admit O(1); serve O(1) (scan at most 5 queues); space O(n). Works because severity has only 5 values and each queue is already in arrival order. Not usable for a continuous priority (e.g. a score).
- **PriorityQueue:** admit O(log n), serve O(log n), space O(n) (single array).
