# Autonomous Vehicle Decision System — Java

## Project Structure

```
AVDecisionSystem/
├── Main.java         — entry point, starts all threads
├── AVTask.java       — task model with priority (BRAKE / STEERING / NAVIGATION)
├── AVScheduler.java  — preemptive priority scheduler + interrupt handler
├── SensorHub.java    — simulates camera, proximity sensor, GPS streams
├── RLAgent.java      — Q-learning agent that adjusts priorities dynamically
└── README.md
```

## How to Compile & Run

```bash
# 1. Create a package directory
mkdir -p av

# 2. Copy all .java files into av/

# 3. Compile from the parent directory
javac av/*.java

# 4. Run
java av.Main
```

## Key Design Decisions

| Concept | Implementation |
|---|---|
| Priority Queue | `PriorityBlockingQueue<AVTask>` (thread-safe min-heap) |
| Preemption | `AtomicBoolean interrupted` flag + `Thread.interrupt()` |
| Interrupt safety | Tasks caught mid-execution are re-queued |
| Concurrent inputs | `ScheduledExecutorService` for each sensor stream |
| RL state | Queue depth (low / mid / high load) |
| RL action | Adjust non-brake task priority by ±1 |
| RL reward | −2 per queued brake task, +1 for empty brake queue |

## Priority Scale

- **1 = BRAKE** (critical, never preempted)
- **2 = STEERING** (medium, preempted by brake)
- **3 = NAVIGATION** (low, preempted by brake or steering)

Lower integer = higher urgency (min-heap ordering).
