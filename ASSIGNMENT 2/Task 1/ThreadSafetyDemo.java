/** Q1 design decision: shared booking log appended to by several threads (separate runs for each class). */
public class ThreadSafetyDemo {
    static final int THREADS = 16, PER_THREAD = 200_000;

    static String run(Appendable2 target, java.util.function.IntSupplier length) throws Exception {
        Thread[] ts = new Thread[THREADS];
        final String[] error = {""};
        for (int i = 0; i < THREADS; i++) ts[i] = new Thread(() -> {
            try { for (int k = 0; k < PER_THREAD; k++) target.add("ab"); }
            catch (Throwable t) { error[0] = " (" + t.getClass().getSimpleName() + ")"; }
        });
        for (Thread t : ts) t.start();
        for (Thread t : ts) t.join();
        return length.getAsInt() + error[0];
    }
    interface Appendable2 { void add(String s); }

    public static void main(String[] args) throws Exception {
        for (int trial = 1; trial <= 10; trial++) {
            StringBuilder sb = new StringBuilder();
            StringBuffer sbuf = new StringBuffer();
            String r1 = run(sb::append, sb::length);
            String r2 = run(sbuf::append, sbuf::length);
            System.out.println("Trial " + trial + ": expected " + 2 * THREADS * PER_THREAD
                    + " | StringBuilder = " + r1 + " | StringBuffer = " + r2);
        }
    }
}
