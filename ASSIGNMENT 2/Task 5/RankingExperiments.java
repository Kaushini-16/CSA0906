import java.util.*;

/** Q5 - own design: Comparator chain, bounded generic Leaderboard, wildcards, faulty comparators, alternatives. */
public class RankingExperiments {
    record Team(String name, int points, int goalDiff) {}
    record Player(String name, int score) implements Comparable<Player> {          // Comparable = ONE natural order
        public int compareTo(Player o) { return Integer.compare(o.score, score); }  // high score first
    }

    /** Reusable and bounded: works for any T that can be ordered; wildcard lets us add a List<? extends T>. */
    static class Board<T> {
        private final List<T> items = new ArrayList<>();
        private final Comparator<? super T> order;
        Board(Comparator<? super T> order) { this.order = order; }
        void addAll(Collection<? extends T> more) { items.addAll(more); }          // PECS: producer extends
        List<T> ranking() { List<T> c = new ArrayList<>(items); c.sort(order); return c; }
        static <E extends Comparable<? super E>> Board<E> natural() { return new Board<>(Comparator.naturalOrder()); }  // bounded type
    }

    static final Comparator<Team> GOOD = Comparator.comparingInt(Team::points).reversed()
            .thenComparing(Comparator.comparingInt(Team::goalDiff).reversed()).thenComparing(Team::name);
    /** Faulty 1: subtraction - overflows for large/negative values. */
    static final Comparator<Team> SUBTRACTION = (a, b) -> b.points() - a.points();
    /** Faulty 2: ignores the name tie-breaker (equal items not totally ordered; output depends on input order). */
    static final Comparator<Team> NO_NAME = Comparator.comparingInt(Team::points).reversed()
            .thenComparing(Comparator.comparingInt(Team::goalDiff).reversed());

    /** Checks the principle for every pair/triple: antisymmetry, transitivity, equal items compare equal. */
    static String check(Comparator<Team> c, List<Team> ts) {
        int antisym = 0, trans = 0;
        for (Team a : ts) for (Team b : ts) {
            if (Integer.signum(c.compare(a, b)) != -Integer.signum(c.compare(b, a))) antisym++;
            for (Team d : ts) if (c.compare(a, b) < 0 && c.compare(b, d) < 0 && c.compare(a, d) >= 0) trans++;
        }
        return "antisymmetry violations=" + antisym + ", transitivity violations=" + trans;
    }

    public static void main(String[] args) {
        List<Team> ties = List.of(new Team("Panthers", 9, 4), new Team("Eagles", 9, 6), new Team("Cobras", 6, 1), new Team("Bisons", 9, 4),
                new Team("Lions", 9, 6), new Team("Hawks", 6, 1), new Team("Wolves", 6, -2), new Team("Tigers", 3, 0));
        Board<Team> b = new Board<>(GOOD);
        b.addAll(ties);                                                               // List<Team> into Board<Team>
        System.out.println("Ties at every level, correct comparator:\n  " + b.ranking());

        // Faulty comparator test data: points far apart so subtraction overflows
        List<Team> extreme = List.of(new Team("Low", Integer.MIN_VALUE + 5, 0), new Team("High", 2_000_000_000, 0), new Team("Mid", 5, 0));
        List<Team> e1 = new ArrayList<>(extreme); e1.sort(SUBTRACTION);
        List<Team> e2 = new ArrayList<>(extreme); e2.sort(GOOD);
        System.out.println("\nOverflow data (points: Integer.MIN_VALUE+5, 2,000,000,000, 5)");
        System.out.println("  subtraction comparator: " + names(e1) + "   <- WRONG (High must be first)");
        System.out.println("  correct comparator    : " + names(e2));
        System.out.println("  " + check(SUBTRACTION, extreme));

        // Ignoring tie-breaker: same data, two input orders
        List<Team> in1 = new ArrayList<>(ties), in2 = new ArrayList<>(ties); Collections.reverse(in2);
        in1.sort(NO_NAME); in2.sort(NO_NAME);
        System.out.println("\nNo name tie-breaker, two input orders of the same data:");
        System.out.println("  order 1: " + names(in1)); System.out.println("  order 2: " + names(in2));
        System.out.println("  identical? " + in1.equals(in2) + "  (a correct ranking must not depend on input order)");
        List<Team> in3 = new ArrayList<>(ties), in4 = new ArrayList<>(ties); Collections.reverse(in4); in3.sort(GOOD); in4.sort(GOOD);
        System.out.println("  with full comparator identical? " + in3.equals(in4));
        System.out.println("  " + check(GOOD, ties));

        // Comparable-based reuse for players
        Board<Player> pb = Board.natural();
        pb.addAll(List.of(new Player("Ravi", 40), new Player("Asha", 55), new Player("Kumar", 40)));
        System.out.println("\nComparable<Player> natural board: " + pb.ranking());

        // Alternative: three stable sorts (name, goal difference, points) vs one comparator chain
        Random r = new Random(5); List<Team> big = new ArrayList<>();
        for (int i = 0; i < 500_000; i++) big.add(new Team("T" + r.nextInt(100_000), r.nextInt(50), r.nextInt(30)));
        double chain = 0, three = 0;
        for (int run = 0; run < 4; run++) {
            List<Team> c = new ArrayList<>(big); long t0 = System.nanoTime(); c.sort(GOOD); double a = (System.nanoTime() - t0) / 1e6;
            List<Team> s = new ArrayList<>(big); t0 = System.nanoTime();
            s.sort(Comparator.comparing(Team::name)); s.sort(Comparator.comparingInt(Team::goalDiff).reversed()); s.sort(Comparator.comparingInt(Team::points).reversed());
            double t = (System.nanoTime() - t0) / 1e6;
            if (run > 0) { chain += a; three += t; }
            if (run == 3) System.out.println("\n500,000 teams: results identical = " + c.equals(s));
        }
        System.out.printf("One comparator chain: %.0f ms (1 sort)   Three stable sorts: %.0f ms (3 sorts)%n", chain / 3, three / 3);
    }
    static String names(List<Team> l) { StringBuilder sb = new StringBuilder(); for (Team t : l) sb.append(t.name()).append('(').append(t.points()).append(',').append(t.goalDiff()).append(") "); return sb.toString().trim(); }
}
