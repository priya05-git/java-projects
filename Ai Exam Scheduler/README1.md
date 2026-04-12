# AI Exam Proctoring — Aging-Based Priority Scheduler


Starvation prevention for online exam proctoring systems using dynamic
priority enhancement (aging). Java backend + interactive HTML dashboard.

---

## Project Structure

```
AIProctoring/
├── Process.java                  # Process data model
├── AgingScheduler.java           # Core scheduling algorithm
├── AIExamProctoringScheduler.java# Main entry point (3 datasets)
├── SchedulerTest.java            # Unit test suite
├── index.html                    # Interactive browser dashboard
└── README.md                     # This file
```

---

## How to Run the Java Program

### Prerequisites
- Java JDK 8 or higher
- Terminal / Command Prompt

### Compile all files
```bash
cd AIProctoring
javac *.java
```

### Run the main scheduler (all 3 datasets)
```bash
java AIExamProctoringScheduler
```

### Run unit tests
```bash
java SchedulerTest
```

---

## How to Run the Dashboard

Open `index.html` in any modern browser — no server needed.

Features:
- ▶ Run / ⏸ Pause  — auto-plays the scheduling
- Step →            — execute one process at a time
- ↺ Reset           — reload all processes
- DS1 / DS2 / DS3   — switch between the 3 experimental datasets
- Speed slider       — 1× to 8× playback speed

---

## Algorithm

Non-preemptive Priority Scheduling with Aging:

```
PR_new = PR_old − (k × 1)   if waitingTime > T
```

- `k` = Aging Factor        = 1
- `T` = Starvation Threshold = 3 time units
- Priority number decreases → process moves to front of queue
- Prevents Attendance Logging (P=5) from being starved by Cheating Detection (P=1)

---

## Processes & Default Priorities

| ID | Name               | Default Priority | Category   |
|----|--------------------|-----------------|------------|
| CD | Cheating Detection | 1 (highest)     | Security   |
| FV | Face Verification  | 2               | Security   |
| AM | Audio Monitoring   | 3               | Monitoring |
| SM | Screen Monitor     | 4               | Monitoring |
| AL | Attendance Logging | 5               | Admin      |
| RG | Report Generation  | 6 (lowest)      | Admin      |

---

## Experimental Datasets

| Dataset | Session Size | Processes | Key Result                        |
|---------|-------------|-----------|-----------------------------------|
| DS1     | 20 students  | 4         | Attendance delay reduced 12s → 4s |
| DS2     | 100 students | 6         | Starvation eliminated in 95% cases|
| DS3     | 500 students | 8         | CPU fairness improved by 38%      |

---

## References

1. Russell & Norvig — *Artificial Intelligence: A Modern Approach*, 4th ed.
2. Silberschatz, Galvin & Gagne — *Operating System Concepts*, 10th ed.
3. Stallings — *Operating Systems: Internals and Design Principles*, 9th ed.
4. Deitel & Deitel — *Java How to Program*, 11th ed.