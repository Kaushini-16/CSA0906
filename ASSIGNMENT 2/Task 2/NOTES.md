# Q2 - Notes (ArrayList vs LinkedList vs ArrayDeque vs manual array)

Experiments: `ListBenchmark.java` - sizes 10,000 / 50,000 / 100,000; 1,000 operations per measurement (add-at-end: n operations); first run discarded as warm-up, 5 runs averaged; raw numbers in `raw_data.csv`, table in `output.txt`.

## Complexity table (expected) and what was observed
| Operation | ArrayList | LinkedList | ArrayDeque | Plain array (manual shift) | Observed at n=100,000 (us/op) AL / LL / AD |
|---|---|---|---|---|---|
| add at end | O(1) amortised | O(1) | O(1) amortised | O(1) amortised | 0.013 / 0.011 / 0.009 |
| insert at front | O(n) | O(1) | O(1) | O(n) | 7.8 / 0.011 / 0.005 |
| insert in middle | O(n) | O(n) (walk to node) | not supported | O(n) | 3.95 / 123.9 / n/a |
| get k-th | O(1) | O(n) | not supported | O(1) | 0.016 / 64.6 / n/a |
| iterate all | O(n) | O(n) | O(n) | O(n) | 1.3 / 0.8 / 0.23 ms (small differences) |
| space per element | 1 array slot (4-8 bytes, spare capacity up to ~50%) | node: 24-byte object + 2 references (about 24-40 bytes) | 1 array slot | 1 slot |  |

Key observations:
- LinkedList's front insert is fast, but **middle insert/remove and get(k) are much slower than ArrayList** because it must walk to the node (0.016 vs 64.6 us for get(k) at n=100,000, about 4,000x). The "O(1) insert" of LinkedList is only true *once you already hold the node/iterator position*.
- ArrayList's middle insert is O(n) but uses a fast `System.arraycopy`, so it beat LinkedList at every size, and the manual-shift loop was about 5x slower than `ArrayList` (JIT-optimised memmove vs element-by-element loop).
- Timings are noisy for small n (JIT, GC) - read the growth with n, not single values.

## Operating boundary: when ArrayList is reasonable, and when to stop recommending it
ArrayList is a sound choice when the playlist is mostly **appended to, read by position and iterated**, with only occasional inserts/removes in the middle. Insert/remove cost grows linearly: about 4 us (middle) at 100,000 songs is still fine for a user clicking a button. I would stop recommending it when there are **frequent front insertions/removals** (queue-like use) or tens of thousands of such operations per second on large lists - the cost then scales as n per operation (7.8 us each at n=100,000, i.e. 0.78 s for 100,000 operations).

## Changed condition - "Up Next" queue
Pattern: add at both ends, remove from the front, never index. The benchmark ran 200,000 mixed operations: ArrayList 339 ms (O(n) per front operation), LinkedList 3.2 ms, ArrayDeque 14.8 ms. ArrayList is clearly wrong here. **ArrayDeque is the recommended choice**: O(1) amortised at both ends, no per-element node objects, better cache locality. In this single run LinkedList was faster than ArrayDeque; that difference is within JIT/GC noise at this scale and I would repeat with more runs before relying on it - the engineering argument for ArrayDeque is memory (no 24-byte node per song) and no pointer chasing. Switch condition: if the app also needs "jump to k-th song" in the same structure, keep ArrayList for the playlist and ArrayDeque only for the Up Next queue.

## Alternative - plain array with manual shifting
Same complexity as ArrayList (O(1) get/append amortised, O(n) insert/remove) but you must write the resizing and shifting yourself; measured about 5x slower than `ArrayList.add(index, e)` and error-prone. ArrayList is that solution, already optimised.
