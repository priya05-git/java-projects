package av;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Reinforcement Learning Agent — dynamic priority adjustment.
 *
 * Simplified Q-learning loop:
 *   State   : queue depth + current task type
 *   Action  : bump priority of waiting tasks up or down
 *   Reward  : penalty for brake delay, bonus for throughput
 *
 * The agent runs on its own thread, periodically observing the queue
 * and updating task priorities before they are consumed by the scheduler.
 */
public class RLAgent implements Runnable {

    private final AVScheduler scheduler;

    // Q-table: rows = state (0=low load, 1=mid, 2=high),
    //          cols = action (-1=lower priority, 0=keep, +1=raise priority)
    private final double[][] qTable = {
        {0.0,  0.5,  0.2},   // low load
        {0.1,  0.3,  0.8},   // mid load
        {0.5,  0.2,  1.0}    // high load — always raise priorities
    };

    private static final double ALPHA   = 0.1;   // learning rate
    private static final double GAMMA   = 0.9;   // discount factor
    private static final double EPSILON = 0.15;  // exploration rate

    private double totalReward = 0;
    private int    episodes    = 0;

    public RLAgent(AVScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        System.out.println("[RL AGENT] Online — learning priority adjustments.\n");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                TimeUnit.MILLISECONDS.sleep(600);  // observe every 600 ms
                adjustPriorities();
                if (++episodes % 5 == 0) {
                    System.out.printf("  [RL] Episode %d | cumulative reward: %.2f%n",
                            episodes, totalReward);
                }
            }
        } catch (InterruptedException ignored) {}
        System.out.println("[RL AGENT] Offline.");
    }

    // ─── Core RL loop ──────────────────────────────────────────────────────────

    private void adjustPriorities() {
        PriorityBlockingQueue<AVTask> queue = scheduler.getTaskQueue();
        int queueDepth = queue.size();
        int state = queueDepth <= 2 ? 0 : queueDepth <= 5 ? 1 : 2;

        // ε-greedy action selection
        int action = selectAction(state);   // -1, 0, or +1

        if (action != 0 && !queue.isEmpty()) {
            // Snapshot to iterate safely (PBQ doesn't support indexed access)
            AVTask[] tasks = queue.toArray(new AVTask[0]);
            for (AVTask task : tasks) {
                // Never raise brake priority beyond 1 (already max)
                // Never lower brake priority
                if (task.getType() == AVTask.Type.BRAKE) continue;

                int oldPriority = task.getPriority();
                int newPriority = clamp(oldPriority - action, 1, 10);
                // Note: lowering the int = higher urgency (min-heap)
                task.setPriority(newPriority);

                if (oldPriority != newPriority) {
                    System.out.printf("  [RL] %s priority %d → %d (state=%d, action=%+d)%n",
                            task.getType().label, oldPriority, newPriority, state, action);
                }
            }
        }

        // Simulate reward: negative for brake backlog, positive for throughput
        long brakeCount = queue.stream()
                .filter(t -> t.getType() == AVTask.Type.BRAKE).count();
        double reward = (brakeCount == 0 ? 1.0 : -2.0 * brakeCount) + (action == 1 ? 0.3 : 0);

        updateQTable(state, actionIndex(action), reward, state);
        totalReward += reward;
    }

    // ─── Q-learning update ─────────────────────────────────────────────────────

    private void updateQTable(int state, int actionIdx, double reward, int nextState) {
        double best = bestQ(nextState);
        qTable[state][actionIdx] += ALPHA * (reward + GAMMA * best - qTable[state][actionIdx]);
    }

    private int selectAction(int state) {
        if (Math.random() < EPSILON) {
            return randomAction();
        }
        double[] row = qTable[state];
        int best = 0;
        for (int i = 1; i < row.length; i++) if (row[i] > row[best]) best = i;
        return indexToAction(best);  // convert index 0/1/2 → action -1/0/+1
    }

    private double bestQ(int state) {
        double max = Double.NEGATIVE_INFINITY;
        for (double v : qTable[state]) if (v > max) max = v;
        return max;
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private int randomAction() {
        return new int[]{-1, 0, 1}[(int)(Math.random() * 3)];
    }

    private int actionIndex(int action) { return action + 1; }    // -1→0, 0→1, 1→2
    private int indexToAction(int idx)  { return idx - 1; }       //  0→-1, 1→0, 2→1

    private int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}
