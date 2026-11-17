public class WalletTransfer {
    static class Wallet {
        final int id;
        int balance;
        Wallet(int id, int balance) { this.id = id; this.balance = balance; }
    }

    // Always acquire locks in ascending wallet-id order to prevent circular wait.
    static void transfer(Wallet from, Wallet to, int amount) {
        Wallet first = from.id < to.id ? from : to;
        Wallet second = from.id < to.id ? to : from;
        synchronized (first) {
            synchronized (second) {
                if (amount > 0 && from.balance >= amount) {
                    from.balance -= amount;
                    to.balance += amount;
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Wallet a = new Wallet(1, 1000);
        Wallet b = new Wallet(2, 1000);
        Thread t1 = new Thread(() -> { for (int i = 0; i < 1000; i++) transfer(a, b, 1); });
        Thread t2 = new Thread(() -> { for (int i = 0; i < 1000; i++) transfer(b, a, 1); });
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.println("Total wallet balance: " + (a.balance + b.balance));
    }
}
