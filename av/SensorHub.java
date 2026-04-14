package av;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SensorHub simulates three input streams:
 *   - Camera  → may detect obstacles → submits BRAKE tasks
 *   - Sensor  → proximity / LiDAR   → submits BRAKE or STEERING tasks
 *   - GPS     → route data           → submits NAVIGATION tasks
 *
 * Each stream fires on its own schedule to mimic real async hardware input.
 */
public class SensorHub implements Runnable {

    private final AVScheduler scheduler;
    private final Random rng = new Random();
    private ScheduledExecutorService executor;

    public SensorHub(AVScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        executor = Executors.newScheduledThreadPool(3);
        System.out.println("[SENSOR HUB] All sensor streams active.\n");

        // Camera fires every 1.5 s — occasionally spots an obstacle
        executor.scheduleAtFixedRate(this::cameraFeed, 500, 1500, TimeUnit.MILLISECONDS);

        // Proximity sensor fires every 800 ms
        executor.scheduleAtFixedRate(this::proximityFeed, 300, 800, TimeUnit.MILLISECONDS);

        // GPS fires every 2 s — pure navigation updates
        executor.scheduleAtFixedRate(this::gpsFeed, 1000, 2000, TimeUnit.MILLISECONDS);

        // Keep this thread alive (it's a daemon so JVM won't hang on it)
        try { executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS); }
        catch (InterruptedException ignored) {}
    }

    // ─── Sensor callbacks ──────────────────────────────────────────────────────

    private void cameraFeed() {
        if (rng.nextInt(100) < 25) {   // 25% chance of obstacle
            System.out.println("  [CAMERA] ⚠ Obstacle detected!");
            scheduler.submit(new AVTask(AVTask.Type.BRAKE, "Camera"));
        } else {
            System.out.println("  [CAMERA] Lane clear.");
        }
    }

    private void proximityFeed() {
        int distance = 10 + rng.nextInt(90);  // 10–100 m
        System.out.printf("  [SENSOR] Proximity: %d m%n", distance);
        if (distance < 30) {
            System.out.println("  [SENSOR] ⚠ Close proximity — braking!");
            scheduler.submit(new AVTask(AVTask.Type.BRAKE, "Sensor"));
        } else if (distance < 60) {
            System.out.println("  [SENSOR] Steering correction needed.");
            scheduler.submit(new AVTask(AVTask.Type.STEERING, "Sensor"));
        }
    }

    private void gpsFeed() {
        System.out.println("  [GPS] Navigation waypoint received.");
        scheduler.submit(new AVTask(AVTask.Type.NAVIGATION, "GPS"));
    }

    public void shutdown() {
        if (executor != null) executor.shutdownNow();
    }
}
