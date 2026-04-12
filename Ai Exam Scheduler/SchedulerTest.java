import java.util.*;

/**
 * SchedulerTest.java
 * -----------------------------------------------------------------------
 * Unit tests for the AgingScheduler.
 * Run with: javac *.java && java SchedulerTest
 * -----------------------------------------------------------------------
 */
public class SchedulerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           SCHEDULER UNIT TEST SUITE                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        testProcessInit();
        testSingleProcess();
        testPriorityOrder();
        testAgingTriggered();
        testNoStarvationWhenAgingWorks();
        testCompletionTime();

        System.out.println("\n── Results ──────────────────────────────────────────────");
        System.out.printf("  Passed: %d  |  Failed: %d  |  Total: %d%n",
            passed, failed, passed + failed);
        System.out.println("──────────────────────────────────────────────────────────");
        if (failed == 0) System.out.println("  ✅  ALL TESTS PASSED");
        else             System.out.println("  ❌  SOME TESTS FAILED");
    }

    // -----------------------------------------------------------------------
    // Individual tests
    // -----------------------------------------------------------------------

    static void testProcessInit() {
        Process p = new Process("T1", "Test Process", 5, 3);
        assertTrue("Initial priority is 3",           p.getPriority() == 3);
        assertTrue("Original priority is 3",          p.getOriginalPriority() == 3);
        assertTrue("Initial wait is 0",               p.getWaitingTime() == 0);
        assertTrue("Initially not completed",         !p.isCompleted());
        assertTrue("Aging boosts start at 0",         p.getAgingBoosts() == 0);
        System.out.println("[PASS] testProcessInit");
    }

    static void testSingleProcess() {
        List<Process> procs = Arrays.asList(
            new Process("P1", "Solo Task", 4, 1)
        );
        AgingScheduler scheduler = new AgingScheduler(procs);
        // Redirect output quietly
        scheduler.run();
        assertTrue("Single process completes",  procs.get(0).isCompleted());
        assertTrue("Completion time = burst",   procs.get(0).getCompletionTime() == 4);
        assertTrue("Total time = 4",            scheduler.getTotalTime() == 4);
        System.out.println("[PASS] testSingleProcess");
    }

    static void testPriorityOrder() {
        // Lower priority number should execute first
        Process p1 = new Process("P1", "High Priority", 2, 1);
        Process p2 = new Process("P2", "Low Priority",  2, 5);
        List<Process> procs = Arrays.asList(p2, p1); // intentionally reversed
        AgingScheduler scheduler = new AgingScheduler(procs);
        scheduler.run();

        // p1 (priority=1) must start before p2 (priority=5)
        assertTrue("High-priority task starts first", p1.getStartTime() < p2.getStartTime());
        System.out.println("[PASS] testPriorityOrder");
    }

    static void testAgingTriggered() {
        // AL has low priority but must be boosted after waiting > threshold
        Process cd = new Process("CD", "Cheating Detection",  6, 1);
        Process al = new Process("AL", "Attendance Logging",  3, 5);
        List<Process> procs = Arrays.asList(cd, al);
        AgingScheduler scheduler = new AgingScheduler(procs);
        scheduler.run();

        // AL waited 6 units (burst of CD) which is > threshold=3 → should have aged
        assertTrue("Attendance Logging was aged at least once", al.getAgingBoosts() >= 1);
        assertTrue("Priority improved from original",           al.getPriority() < al.getOriginalPriority());
        System.out.println("[PASS] testAgingTriggered");
    }

    static void testNoStarvationWhenAgingWorks() {
        // All processes must eventually complete — no infinite starvation
        List<Process> procs = Arrays.asList(
            new Process("CD", "Cheating Detection", 5, 1),
            new Process("FV", "Face Verification",  3, 2),
            new Process("AM", "Audio Monitoring",   2, 3),
            new Process("SM", "Screen Monitor",     4, 4),
            new Process("AL", "Attendance Logging", 4, 5),
            new Process("RG", "Report Generation",  3, 6)
        );
        AgingScheduler scheduler = new AgingScheduler(procs);
        scheduler.run();

        for (Process p : procs) {
            assertTrue(p.getName() + " completed", p.isCompleted());
        }
        System.out.println("[PASS] testNoStarvationWhenAgingWorks");
    }

    static void testCompletionTime() {
        // With two sequential processes, total time = sum of bursts
        Process p1 = new Process("P1", "Task A", 3, 1);
        Process p2 = new Process("P2", "Task B", 4, 2);
        List<Process> procs = Arrays.asList(p1, p2);
        AgingScheduler scheduler = new AgingScheduler(procs);
        scheduler.run();

        assertTrue("Total time = 7", scheduler.getTotalTime() == 7);
        assertTrue("Task B completion = 7", p2.getCompletionTime() == 7);
        System.out.println("[PASS] testCompletionTime");
    }

    // -----------------------------------------------------------------------
    // Assertion helper
    // -----------------------------------------------------------------------
    static void assertTrue(String message, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("  [FAIL] " + message);
        }
    }
}