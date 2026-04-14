package av;

/**
 * Represents a vehicle control task with preemptive priority.
 * Lower priority number = higher urgency (1 = critical brake).
 */
public class AVTask implements Comparable<AVTask> {

    public enum Type {
        BRAKE(1, "BRAKE", "🔴"),
        STEERING(2, "STEERING", "🟡"),
        NAVIGATION(3, "NAVIGATION", "🟢");

        public final int defaultPriority;
        public final String label;
        public final String icon;

        Type(int defaultPriority, String label, String icon) {
            this.defaultPriority = defaultPriority;
            this.label = label;
            this.icon = icon;
        }
    }

    private final Type type;
    private volatile int priority;         // dynamic: RL agent can modify this
    private final String sourceInput;      // which sensor triggered this
    private final long createdAt;

    public AVTask(Type type, String sourceInput) {
        this.type = type;
        this.priority = type.defaultPriority;
        this.sourceInput = sourceInput;
        this.createdAt = System.currentTimeMillis();
    }

    public Type getType() { return type; }
    public int getPriority() { return priority; }
    public String getSourceInput() { return sourceInput; }
    public long getCreatedAt() { return createdAt; }

    /** RL agent calls this to dynamically bump priority. */
    public void setPriority(int priority) {
        this.priority = Math.max(1, Math.min(10, priority));
    }

    @Override
    public int compareTo(AVTask other) {
        // Lower number = higher priority (min-heap behavior)
        return Integer.compare(this.priority, other.priority);
    }

    @Override
    public String toString() {
        return String.format("[%s P%d from %s]", type.icon + " " + type.label, priority, sourceInput);
    }
}
