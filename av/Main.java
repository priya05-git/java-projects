package av;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Autonomous Vehicle Decision System ===\n");

        AVScheduler scheduler = new AVScheduler();
        RLAgent rlAgent = new RLAgent(scheduler);
        SensorHub sensorHub = new SensorHub(scheduler);

        // Start all threads
        Thread schedulerThread = new Thread(scheduler, "Scheduler");
        Thread rlThread = new Thread(rlAgent, "RL-Agent");
        Thread sensorThread = new Thread(sensorHub, "Sensor-Hub");

        schedulerThread.setDaemon(true);
        rlThread.setDaemon(true);
        sensorThread.setDaemon(true);

        schedulerThread.start();
        rlThread.start();
        sensorThread.start();

        // Run for 10 seconds then shutdown
        Thread.sleep(10_000);
        System.out.println("\n[SYSTEM] Shutting down AV Decision System...");
        scheduler.shutdown();
    }
}
