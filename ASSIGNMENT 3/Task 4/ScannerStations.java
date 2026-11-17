class ScannerStation extends Thread {
    private final int stationId;
    private final int scripts;

    ScannerStation(int stationId, int scripts) {
        super("Station-" + stationId);
        this.stationId = stationId;
        this.scripts = scripts;
    }

    @Override
    public void run() {
        process();
    }

    private void process() {
        try {
            for (int i = 1; i <= scripts; i++) {
                Thread.sleep(100L * stationId); // simulate scanning work
            }
            System.out.println("Station " + stationId + " scanned " + scripts + " scripts.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println(getName() + " was interrupted.");
        }
    }
}

public class ScannerStations {
    public static void main(String[] args) throws InterruptedException {
        Thread[] stations = new Thread[4];
        for (int i = 0; i < stations.length; i++) {
            stations[i] = new ScannerStation(i + 1, 5);
            stations[i].start();
        }

        for (Thread station : stations) {
            System.out.println(station.getName() + " alive: " + station.isAlive());
            station.join();
        }
        System.out.println("All stations completed. Total scripts scanned: 20");
    }
}
