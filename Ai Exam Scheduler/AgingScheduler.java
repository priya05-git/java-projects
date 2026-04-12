import java.util.*;

/**
 * AgingScheduler.java
 * -----------------------------------------------------------------------
 * Core scheduling engine for the AI Exam Proctoring System.
 *
 * Algorithm:
 *   - Non-preemptive Priority Scheduling
 *   - Aging technique: every time a process waits > STARVATION_THRESHOLD,
 *     its effective priority is incremented by AGING_FACTOR
 *     (priority number decreases → moves closer to front of queue)
 *
 * PR_new = PR_old - (k × 1)  if waitingTime > T
 *   where k = AGING_FACTOR, T = STARVATION_THRESHOLD
 * -----------------------------------------------------------------------
 */
public class AgingScheduler {

    // -----------------------------------------------------------------------
    // Tuning constants
    // -----------------------------------------------------------------------
    /** Aging factor k: priority boost per aging event */
    private static final int AGING_FACTOR = 1;

    /** Starvation threshold T: max wait before aging kicks in */
    private static final int STARVATION_THRESHOLD = 3;

    // -----------------------------------------------------------------------
    // Internal state
    // -----------------------------------------------------------------------
    private List<Process>              processes;
    private List<String>               executionLog;
    private Map<String, List<Integer>> ganttTimeline;   // processId → list of time slots
    private int                        currentTime;
    private int                        starvationEvents;
    private int                        totalAgingBoosts;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------
    public AgingScheduler(List<Process> processes) {
        this.processes       = new ArrayList<>(processes);
        this.executionLog    = new ArrayList<>();
        this.ganttTimeline   = new LinkedHashMap<>();
        this.currentTime     = 0;
        this.starvationEvents = 0;
        this.totalAgingBoosts = 0;

        // Initialise gantt slots for each process
        for (Process p : processes) {
            ganttTimeline.put(p.getId(), new ArrayList<>());
        }
    }

    // -----------------------------------------------------------------------
    // Main scheduling method — runs until all processes are complete
    // -----------------------------------------------------------------------
    public void run() {
        log("=== AI Exam Proctoring Scheduler Started ===");
        log(String.format("Starvation threshold T = %d | Aging factor k = %d",
                STARVATION_THRESHOLD, AGING_FACTOR));
        log("--------------------------------------------");

        while (hasIncompleteProcesses()) {

            // 1. Get the ready queue — sort by effective priority (ties broken by original priority)
            List<Process> readyQueue = getSortedReadyQueue();

            if (readyQueue.isEmpty()) break;

            // 2. Pick the highest-priority process (lowest priority number)
            Process current = readyQueue.get(0);

            // 3. Record start time
            current.setStartTime(currentTime);
            log(String.format("[t=%3d] EXECUTE  %-25s | Priority=%d | Burst=%d | WaitedFor=%d",
                    currentTime, current.getName(), current.getPriority(),
                    current.getBurstTime(), current.getWaitingTime()));

            // 4. Simulate execution — fill Gantt chart
            for (int t = 0; t < current.getBurstTime(); t++) {
                ganttTimeline.get(current.getId()).add(currentTime + t);
            }

            // 5. Advance clock
            currentTime += current.getBurstTime();
            current.setCompletionTime(currentTime);
            current.setCompleted(true);

            // 6. Update waiting times and apply aging to remaining processes
            for (Process p : processes) {
                if (p.isCompleted() || p == current) continue;

                int addedWait = current.getBurstTime();
                p.setWaitingTime(p.getWaitingTime() + addedWait);

                // Check starvation condition
                if (p.getWaitingTime() > STARVATION_THRESHOLD) {
                    int oldPriority = p.getPriority();
                    int newPriority = Math.max(1, oldPriority - AGING_FACTOR);

                    if (newPriority < oldPriority) {
                        p.setPriority(newPriority);
                        p.setAgingBoosts(p.getAgingBoosts() + 1);
                        totalAgingBoosts++;

                        if (oldPriority == p.getOriginalPriority()) {
                            starvationEvents++; // first boost = caught starvation
                        }

                        log(String.format(
                            "         AGING    %-25s | P%d → P%d (waited=%d, boosts=%d)",
                            p.getName(), oldPriority, newPriority,
                            p.getWaitingTime(), p.getAgingBoosts()));
                    }
                }
            }

            log(""); // blank line between executions
        }

        log("=== Scheduling Complete ===");
        log(String.format("Total time     : %d units", currentTime));
        log(String.format("Starvation evts: %d", starvationEvents));
        log(String.format("Aging boosts   : %d", totalAgingBoosts));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private boolean hasIncompleteProcesses() {
        return processes.stream().anyMatch(p -> !p.isCompleted());
    }

    private List<Process> getSortedReadyQueue() {
        List<Process> queue = new ArrayList<>();
        for (Process p : processes) {
            if (!p.isCompleted()) queue.add(p);
        }
        // Primary sort: effective priority (ascending = highest priority first)
        // Secondary sort: original priority (stable tiebreaker)
        queue.sort(Comparator
            .comparingInt(Process::getPriority)
            .thenComparingInt(Process::getOriginalPriority));
        return queue;
    }

    private void log(String message) {
        executionLog.add(message);
        System.out.println(message);
    }

    // -----------------------------------------------------------------------
    // Results & Statistics
    // -----------------------------------------------------------------------
    public void printResults() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           SCHEDULING RESULTS — METRICS SUMMARY           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        int totalWT = 0, totalTAT = 0;

        System.out.printf("%-25s %6s %8s %9s %9s %7s%n",
            "Process", "Burst", "Priority", "WaitTime", "Turnaround", "Boosts");
        System.out.println("-".repeat(68));

        for (Process p : processes) {
            System.out.printf("%-25s %6d %8d %9d %9d %7d%n",
                p.getName(),
                p.getBurstTime(),
                p.getPriority(),
                p.getWaitingTime(),
                p.getTurnaroundTime(),
                p.getAgingBoosts());
            totalWT  += p.getWaitingTime();
            totalTAT += p.getTurnaroundTime();
        }

        System.out.println("-".repeat(68));
        System.out.printf("Average Waiting Time    : %.2f%n", (double) totalWT  / processes.size());
        System.out.printf("Average Turnaround Time : %.2f%n", (double) totalTAT / processes.size());
        System.out.printf("Total Starvation Events : %d%n",   starvationEvents);
        System.out.printf("Total Aging Boosts      : %d%n",   totalAgingBoosts);
        System.out.printf("CPU Utilisation         : 100%% (no idle time)%n");
    }

    public void printGanttChart() {
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                     GANTT CHART                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        for (Process p : processes) {
            List<Integer> slots = ganttTimeline.get(p.getId());
            if (slots.isEmpty()) continue;

            int start = slots.get(0);
            int end   = slots.get(slots.size() - 1) + 1;

            // Draw bar
            StringBuilder bar = new StringBuilder();
            bar.append("|");
            for (int i = 0; i < (end - start) * 2; i++) bar.append("█");
            bar.append("|");

            System.out.printf("%-22s [%3d - %3d]  %s%n",
                p.getName(), start, end, bar.toString());
        }

        System.out.println("\nTimeline: 0 ─────────────────────────────────────► " + currentTime);
    }

    // -----------------------------------------------------------------------
    // Getters for external use
    // -----------------------------------------------------------------------
    public List<String>               getExecutionLog()  { return executionLog; }
    public Map<String, List<Integer>> getGanttTimeline() { return ganttTimeline; }
    public int                        getTotalTime()      { return currentTime; }
    public int                        getStarvationEvents(){ return starvationEvents; }
    public int                        getTotalAgingBoosts(){ return totalAgingBoosts; }
}