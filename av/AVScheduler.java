package av;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Preemptive Priority Scheduler
 *
 * Uses a PriorityBlockingQueue (min-heap) so highest-priority tasks
 * (lowest priority number) are always processed first.
 *
 * Interrupt mechanism: If a BRAKE task arrives while a lower-priority
 * task is executing, the current task is preempted immediately.
 */
public class AVScheduler implements Runnable {

    private final PriorityBlockingQueue<AVTask> taskQueue = new PriorityBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicBoolean interrupted = new AtomicBoolean(false);

    private volatile AVTask currentTask = null;
    private volatile Thread executionThread = null;

    // ─── Submit ────────────────────────────────────────────────────────────────

    /**
     * Submits a new task. If it's a BRAKE task and something lower-priority
     * is currently executing, the interrupt flag fires.
     */
    public void submit(AVTask task) {
        taskQueue.offer(task);
        System.out.printf("  [QUEUE] Enqueued %s (queue size: %d)%n", task, taskQueue.size());

        if (task.getType() == AVTask.Type.BRAKE) {
            triggerInterrupt(task);
        }
    }

    // ─── Interrupt ─────────────────────────────────────────────────────────────

    private void triggerInterrupt(AVTask incoming) {
        AVTask current = currentTask;
        if (current != null && current.getPriority() > incoming.getPriority()) {
            System.out.printf("  [INTERRUPT] ⚡ Preempting %s for critical %s%n", current, incoming);
            interrupted.set(true);
            Thread t = executionThread;
            if (t != null) t.interrupt();
        }
    }

    // ─── Execution loop ────────────────────────────────────────────────────────

    @Override
    public void run() {
        executionThread = Thread.currentThread();
        System.out.println("[SCHEDULER] Started — awaiting tasks...\n");

        while (running.get() || !taskQueue.isEmpty()) {
            try {
                // Block until a task is available
                AVTask task = taskQueue.take();
                currentTask = task;
                interrupted.set(false);

                execute(task);

            } catch (InterruptedException e) {
                // Woken by a higher-priority interrupt — re-check queue top
                Thread.currentThread().interrupt();
                Thread.interrupted(); // clear flag and loop again
            }
        }
        System.out.println("[SCHEDULER] Shutdown complete.");
    }

    private void execute(AVTask task) {
        long start = System.currentTimeMillis();
        System.out.printf("\n[EXEC] ▶ Running %s%n", task);

        try {
            switch (task.getType()) {
                case BRAKE      -> executeBrake(task);
                case STEERING   -> executeSteering(task);
                case NAVIGATION -> executeNavigation(task);
            }
            long elapsed = System.currentTimeMillis() - start;
            System.out.printf("[EXEC] ✓ Completed %s in %dms%n", task.getType().label, elapsed);

        } catch (InterruptedException e) {
            System.out.printf("[EXEC] ⚡ %s preempted after %dms — re-queued%n",
                    task.getType().label, System.currentTimeMillis() - start);
            taskQueue.offer(task);   // put it back for later
            Thread.currentThread().interrupt();
        } finally {
            currentTask = null;
        }
    }

    // ─── Actuator stubs ────────────────────────────────────────────────────────

    private void executeBrake(AVTask task) throws InterruptedException {
        System.out.println("         [BRAKE] Applying emergency brake!");
        simulateWork(100);  // fastest possible — safety critical
    }

    private void executeSteering(AVTask task) throws InterruptedException {
        System.out.println("         [STEERING] Adjusting wheel angle...");
        simulateWork(300);
    }

    private void executeNavigation(AVTask task) throws InterruptedException {
        System.out.println("         [NAVIGATION] Recalculating route...");
        simulateWork(700);
    }

    /**
     * Simulates work in small slices so interrupts are detected promptly.
     */
    private void simulateWork(int totalMs) throws InterruptedException {
        int sliceMs = 50;
        int elapsed = 0;
        while (elapsed < totalMs) {
            if (interrupted.get()) throw new InterruptedException("Preempted");
            Thread.sleep(Math.min(sliceMs, totalMs - elapsed));
            elapsed += sliceMs;
        }
    }

    // ─── Accessors ─────────────────────────────────────────────────────────────

    public PriorityBlockingQueue<AVTask> getTaskQueue() { return taskQueue; }
    public AVTask getCurrentTask() { return currentTask; }

    public void shutdown() {
        running.set(false);
        Thread t = executionThread;
        if (t != null) t.interrupt();
    }
}
