# Q7 - Day-4 Design Challenge: Diagnose, Redesign, Validate
**Chosen solution:** the Q6 triage queue after adding the *aging* fairness rule (PriorityQueue rebuilt on every serve). Evidence: `output.txt`, `raw_data.csv` (code: `AgingTriageComparison.java`).

## A. Diagnose the bottleneck (2)
- **Where the time and memory go:** for every `serve()` the original builds a *new* `PriorityQueue`, re-inserts all n waiting patients (`addAll`) and then removes the winner from the list. Measured per serve: 53 us (n = 2,000), 192 us (10,000), 494 us (50,000); about **n comparator calls per serve** (1,919 / 9,925 / 49,929) and **0.03 / 0.15 / 0.76 MB of garbage per serve** (5.9 / 30 / 153 MB per 200 serves).
- **What grows with data:** the cost of one serve grows with n (O(n log n) worst case, O(n) observed because the list is nearly sorted by arrival), although the answer only ever needs the *one best* patient.
- **Repeated work:** the same relative order of waiting patients is re-derived from scratch at every serve; the heap built at the previous serve is thrown away; plus `list.remove(next)` is another O(n) scan.
- **Cause in the chosen class:** a heap orders by a key that is **fixed when the element is inserted**. Aging makes the key depend on the current time, so the heap invariant becomes stale as time passes and the only way to keep it valid with `PriorityQueue` is to rebuild it.

## B. Design objective (2)
1. A serve must not depend on the number of waiting patients: **O(1) comparisons per serve** (target <= 5 comparisons, measured as comparisons per serve), independent of n.
2. **No garbage proportional to n per serve** (target: approximately 0 MB allocated per serve).
3. **Preserve correctness:** the redesign must serve exactly the same patients in the same order as the original rule (equal severity -> earliest arrival; aged severity = max(1, severity - waited/50)).

## C. Revised solution (4)
```
class LevelQueuesTriage
   ArrayDeque<Patient> q[1..5]            // one FIFO queue per severity level

admit(p):  q[p.severity].addLast(p)                         // O(1)

serve(now):
   best <- none
   FOR level <- 1 TO 5 DO
      h <- q[level].peekFirst()                             // OLDEST patient of this level
      IF h exists AND (best = none OR key(h, now) < key(best-head, now)) THEN best <- level
   RETURN q[best].pollFirst()                               // O(1)
   where key(p, now) = ( max(1, p.severity - (now - p.arrival) DIV STEP) , p.arrival )
```
- **What changed:** the single heap is replaced by 5 FIFO queues (one per severity); priority is evaluated lazily at serve time using the current time instead of being stored.
- **Property exploited:** inside one level all patients share the same base severity, and the aged severity never increases with waiting time, so the patient who arrived first (the head, because the queue is in arrival order) has both the best aged severity *and* the earliest arrival of that level. The best patient overall is therefore one of at most **5 heads** - no sorting, no rebuild, no scan.
- **Operations avoided:** rebuilding the heap (n insertions), the `list.remove` scan, per-serve allocation.
- **Memory/preprocessing:** none extra - still O(n) space (n patients in deques, same as before).
- **New trade-offs:** (a) relies on the rule being monotone in waiting time and on a *small fixed* number of levels; a patient-specific aging rate, or a continuous score, would break the "head is best" property; (b) serve cost is O(L) where L = number of levels (5 here); (c) a full ordered waiting-list display needs an O(n) merge of the 5 queues; (d) comparator flexibility of `PriorityQueue` is lost.

## D. Prediction (made before running the comparison, 1)
Per-serve cost of the original should grow roughly proportionally to n (about n comparisons, n new queue entries); the redesign should stay flat at <= 5 comparisons and show about 0 MB allocated. Expected improvement in: time per serve (orders of magnitude at n = 50,000), comparisons per serve, allocated bytes. At n = 2,000 the gap should be smaller. The two designs must produce identical serve sequences.

## E. Implement and validate (2) - `output.txt`
- **Correctness:** 45,000 serve decisions over 30 random simulations (random arrivals and serves interleaved): the two designs gave **identical sequences: true**.
- **Same conditions (n patients waiting, 200 serves, 3 averaged runs):**

| n | Original: us/serve, comparisons, alloc | Redesign: us/serve, comparisons, alloc |
|---|---|---|
| 2,000 | 53.0, 1,919, 5.9 MB | 0.5, 4, 0.00 MB |
| 10,000 | 192.4, 9,925, 30.1 MB | 0.0 (below timer resolution), 4, 0.00 MB |
| 50,000 | 493.7, 49,929, 152.5 MB | 0.1, 4, 0.00 MB |

## F. Defence (2)
- **Prediction supported?** Yes for comparisons (proportional to n versus constant 4) and memory (proportional to n versus 0). For time, the original grew less than linearly (53 -> 494 us for a 25x larger n) because the JIT and the nearly-sorted input make the heap rebuild cheaper than the worst case; the redesign's sub-microsecond figures are close to the timer's resolution, so the exact ratio is imprecise but the order of magnitude (about 1,000x at n = 50,000) is clear.
- **Magnitude:** about 1,000x less time, 12,000x fewer comparisons, 100% less garbage at n = 50,000.
- **Costs/trade-offs:** see C; the redesign is specific to "levels + monotone aging" and is less general than a comparator.
- **When the original is still preferable:** when the waiting list is small (a few hundred patients - the cost is microseconds), when the priority function is arbitrary (continuous scores, per-patient rates, multi-key rules), or when simplicity and a single well-known class matter more. For the 5,000-patients-a-day hospital in Q6 the original would also work; the redesign is justified when waiting lists reach tens of thousands (mass-casualty events) or serves are very frequent.
