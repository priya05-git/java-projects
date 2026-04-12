/**
 * Process.java
 * Represents a single task in the AI Exam Proctoring Scheduler.
 * Each process has a name, burst time, priority, and aging counter.
 */
public class Process {

    // Unique process identifier
    private String id;

    // Display name of the process
    private String name;

    // CPU burst time required (in time units)
    private int burstTime;

    // Current effective priority (lower number = higher priority)
    private int priority;

    // Original assigned priority (for reference)
    private int originalPriority;

    // Time the process has spent waiting in the ready queue
    private int waitingTime;

    // Number of times aging has boosted this process's priority
    private int agingBoosts;

    // Whether this process has finished execution
    private boolean completed;

    // Time at which execution started (-1 if not started)
    private int startTime;

    // Time at which execution completed (-1 if not done)
    private int completionTime;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    public Process(String id, String name, int burstTime, int priority) {
        this.id               = id;
        this.name             = name;
        this.burstTime        = burstTime;
        this.priority         = priority;
        this.originalPriority = priority;
        this.waitingTime      = 0;
        this.agingBoosts      = 0;
        this.completed        = false;
        this.startTime        = -1;
        this.completionTime   = -1;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------
    public String getId()                        { return id; }
    public String getName()                      { return name; }
    public int    getBurstTime()                 { return burstTime; }
    public int    getPriority()                  { return priority; }
    public int    getOriginalPriority()          { return originalPriority; }
    public int    getWaitingTime()               { return waitingTime; }
    public int    getAgingBoosts()               { return agingBoosts; }
    public boolean isCompleted()                 { return completed; }
    public int    getStartTime()                 { return startTime; }
    public int    getCompletionTime()            { return completionTime; }

    public void setPriority(int priority)        { this.priority = priority; }
    public void setWaitingTime(int waitingTime)  { this.waitingTime = waitingTime; }
    public void setAgingBoosts(int agingBoosts)  { this.agingBoosts = agingBoosts; }
    public void setCompleted(boolean completed)  { this.completed = completed; }
    public void setStartTime(int startTime)      { this.startTime = startTime; }
    public void setCompletionTime(int t)         { this.completionTime = t; }

    // -------------------------------------------------------------------------
    // Turnaround time = completion - arrival (arrival assumed 0 for all)
    // -------------------------------------------------------------------------
    public int getTurnaroundTime() {
        if (completionTime < 0) return -1;
        return completionTime; // arrival time is 0
    }

    // -------------------------------------------------------------------------
    // toString — for clean console output
    // -------------------------------------------------------------------------
    @Override
    public String toString() {
        return String.format(
            "%-25s [ID:%-3s | Burst:%2d | Priority:%2d (orig:%2d) | Wait:%3d | Boosts:%d]",
            name, id, burstTime, priority, originalPriority, waitingTime, agingBoosts
        );
    }
}