# Q5 - Notes (generic ranking)

## Design derived from the principle
- A ranking is correct iff the comparison is a **consistent total order**: antisymmetric (sign(compare(a,b)) = -sign(compare(b,a))), transitive, and `compare == 0` only for items that are genuinely tied.
- `Comparable<Team>` gives ONE natural order baked into the class. A sports table, a "best attack" table and an alphabetical roster need different orders, so **separate `Comparator` objects** are better for Team; `Comparable` is fine for a type with one obvious order (`Player` by score in `RankingExperiments`).
- Multi-field order: `Comparator.comparingInt(points).reversed().thenComparing(goalDiff reversed).thenComparing(name)`. Each stage is a total order on its field and `thenComparing` consults the next field only on ties, so the lexicographic combination is again a total order (transitive). The final name key makes the order total unless two teams share a name.
- Reuse: `Leaderboard<T>` takes `Comparator<? super T>` (a comparator for a supertype can rank subtypes - contravariance); `addAll(Collection<? extends T>)` accepts a `List<Team>` or a list of a subtype (producer-extends); the static factory `<E extends Comparable<? super E>> natural()` uses a **bounded type** so only rankable types compile.

## Complete the Code
`LeaderboardDemo.java` output: `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]` (matches expected, `output.txt`). **Why Bisons is above Panthers:** both have 9 points and goal difference 4, so the first two keys tie; the third key, name A-Z, puts "Bisons" before "Panthers". `ranking()` sorts a *copy*, so the stored list (insertion order) is unchanged.

## Deliberately faulty comparators (`output_experiments.txt`)
- **Subtraction** (`b.points - a.points`): with points MIN_VALUE+5, 2,000,000,000 and 5 the difference overflows; output was `Low High Mid` instead of `High Mid Low`; the pairwise check found 2 antisymmetry and 4 transitivity violations.
- **Missing name tie-breaker:** the same eight teams in two input orders gave different rankings (Eagles/Lions swapped) - equal-looking teams keep input order, so the ranking is not a function of the data. The full comparator gave identical output, with 0 violations.

## Correctness argument (reasoning, not output)
Each key comparison uses `Integer.compare`/`String.compareTo`, which are total orders without overflow. `reversed()` flips a total order into a total order. Lexicographic composition of total orders is a total order: if a < b and b < c, find the first key where a and b differ and the first where b and c differ; the earlier of the two decides a < c, so transitivity holds. Equal on all three keys -> compare is 0 -> the teams are indistinguishable for ranking.

## Alternative concept
- `Comparable<Team>`: a single `compareTo` with the same logic - 1 sort, O(n log n), no extra space beyond the sort buffer, but only one fixed order and harder to reuse for players/colleges (each type needs its own compareTo).
- Three stable sorts (name, then goal difference, then points): 3 x O(n log n) and extra merge buffer each time; measured with 500,000 teams: **one chain about 507 ms vs three sorts about 730 ms**, identical results. Correct only because `List.sort` is stable. The comparator chain is easiest to reuse: write one comparator per ranking rule and pass it to the generic `Leaderboard<T>`.
