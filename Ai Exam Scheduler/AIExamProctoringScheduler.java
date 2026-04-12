import java.util.*;

/**
 * AIExamProctoringScheduler.java
 * -----------------------------------------------------------------------
 * Entry point for the AI Exam Proctoring Scheduling System.
 *
 * Runs three experimental datasets:
 *   Dataset 1 — Small  exam session  (20 students)
 *   Dataset 2 — Medium exam session  (100 students)
 *   Dataset 3 — Large  exam session  (500 students)
 *
 * Each dataset simulates different burst-time profiles and process loads
 * to reproduce the paper's experimental results.
 * -----------------------------------------------------------------------
 */
public class AIExamProctoringScheduler {

    public static void main(String[] args) {

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   AI EXAM PROCTORING — AGING-BASED PRIORITY SCHEDULER   ║");
        System.out.println("║   Starvation Prevention via Dynamic Priority Enhancement ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        runDataset(1, "Small Exam Session (20 Students)",   buildDataset1());
        runDataset(2, "Medium Exam Session (100 Students)", buildDataset2());
        runDataset(3, "Large Exam Session (500 Students)",  buildDataset3());
    }

    // -----------------------------------------------------------------------
    // Run a single dataset through the scheduler
    // -----------------------------------------------------------------------
    private static void runDataset(int num, String label, List<Process> procs) {
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.printf( "║  DATASET %d: %-46s ║%n", num, label);
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        System.out.println("\n[Initial Process Queue]");
        System.out.printf("%-25s %6s %8s%n", "Process", "Burst", "Priority");
        System.out.println("-".repeat(42));
        for (Process p : procs) {
            System.out.printf("%-25s %6d %8d%n",
                p.getName(), p.getBurstTime(), p.getPriority());
        }
        System.out.println();

        AgingScheduler scheduler = new AgingScheduler(procs);
        scheduler.run();
        scheduler.printResults();
        scheduler.printGanttChart();
    }

    // -----------------------------------------------------------------------
    // Dataset 1: Small session — tight burst times, starvation occurs quickly
    // -----------------------------------------------------------------------
    private static List<Process> buildDataset1() {
        return Arrays.asList(
            new Process("CD", "Cheating Detection",   5, 1),
            new Process("FV", "Face Verification",    3, 2),
            new Process("AM", "Audio Monitoring",     2, 3),
            new Process("AL", "Attendance Logging",   4, 5)
        );
    }

    // -----------------------------------------------------------------------
    // Dataset 2: Medium session — more processes, varying burst loads
    // -----------------------------------------------------------------------
    private static List<Process> buildDataset2() {
        return Arrays.asList(
            new Process("CD",  "Cheating Detection",   8, 1),
            new Process("FV",  "Face Verification",    6, 2),
            new Process("AM",  "Audio Monitoring",     4, 3),
            new Process("SM",  "Screen Monitor",       5, 4),
            new Process("AL",  "Attendance Logging",   7, 5),
            new Process("RG",  "Report Generation",    3, 6)
        );
    }

    // -----------------------------------------------------------------------
    // Dataset 3: Large session — heavy burst times, heavy monitoring load
    // -----------------------------------------------------------------------
    private static List<Process> buildDataset3() {
        return Arrays.asList(
            new Process("CD",  "Cheating Detection",  12, 1),
            new Process("FV",  "Face Verification",    9, 2),
            new Process("AM",  "Audio Monitoring",     6, 3),
            new Process("SM",  "Screen Monitor",       8, 4),
            new Process("EM",  "Eye Movement Track",   5, 3),
            new Process("AL",  "Attendance Logging",  10, 5),
            new Process("RG",  "Report Generation",    7, 6),
            new Process("TB",  "Tab-Switch Detect",    4, 2)
        );
    }
}