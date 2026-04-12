import java.util.*;

class ChatbotScheduler {
    static class Request {
        String message;
        int priority;
        String intent;
        double sentiment;
        boolean isUrgent;
        long timestamp;
        String id;

        Request(String m) {
            this.message = m;
            this.timestamp = System.currentTimeMillis();
            this.id = "REQ-" + UUID.randomUUID().toString().substring(0, 8);
            this.intent = classifyIntent(m);
            this.sentiment = analyzeSentiment(m);
            this.isUrgent = detectUrgency(m);
            this.priority = calculatePriority(this.intent, this.sentiment, this.isUrgent);
        }

        static String classifyIntent(String message) {
            String lower = message.toLowerCase();
            if (lower.contains("complaint") || lower.contains("problem") || 
                lower.contains("issue") || lower.contains("broken") || lower.contains("error") ||
                lower.contains("not working") || lower.contains("failed")) {
                return "COMPLAINT";
            } else if (lower.contains("feedback") || lower.contains("suggest") ||
                       lower.contains("improve") || lower.contains("good") || lower.contains("excellent") ||
                       lower.contains("feature request")) {
                return "FEEDBACK";
            } else if (lower.contains("urgent") || lower.contains("asap") || 
                       lower.contains("critical") || lower.contains("emergency")) {
                return "URGENT_QUERY";
            } else {
                return "GENERAL_QUERY";
            }
        }

        static double analyzeSentiment(String message) {
            String lower = message.toLowerCase();
            double score = 0.0;
            
            String[] positive = {"good", "great", "excellent", "thanks", "helpful", "thank you", 
                                "amazing", "wonderful", "love", "perfect", "outstanding"};
            String[] negative = {"bad", "poor", "awful", "terrible", "problem", "issue", 
                                "complaint", "angry", "frustrated", "unhappy", "disgusted"};
            
            for (String word : positive) {
                if (lower.contains(word)) score += 0.25;
            }
            for (String word : negative) {
                if (lower.contains(word)) score -= 0.25;
            }
            
            return Math.max(-1.0, Math.min(1.0, score));
        }

        static boolean detectUrgency(String message) {
            String lower = message.toLowerCase();
            String[] urgencyKeywords = {"urgent", "asap", "critical", "emergency", "immediately", 
                                       "right now", "can't wait", "help please", "breaking", "down"};
            for (String keyword : urgencyKeywords) {
                if (lower.contains(keyword)) return true;
            }
            return false;
        }

        static int calculatePriority(String intent, double sentiment, boolean isUrgent) {
            if (isUrgent) return 1;  // CRITICAL - Urgent queries
            if (intent.equals("COMPLAINT")) return 2;  // HIGH - Complaints
            if (intent.equals("URGENT_QUERY")) return 3;  // HIGH - Urgent queries
            if (sentiment < -0.5) return 4;  // MEDIUM - Very negative sentiment
            if (intent.equals("GENERAL_QUERY")) return 5;  // MEDIUM-LOW
            if (sentiment > 0.3) return 7;  // LOW - Positive feedback
            return 6;  // DEFAULT
        }
    }

    public static void main(String[] args) {
        // Simulate thousands of requests
        Request[] requests = {
            new Request("URGENT: System is completely down! Can't access anything!"),
            new Request("Complaint about slow response times"),
            new Request("Great feedback on your service"),
            new Request("General query about account features"),
            new Request("CRITICAL: Database connection failed - immediate help needed"),
            new Request("Problem with login not working"),
            new Request("Good suggestions for improvement"),
            new Request("Feedback: Love your new UI design"),
            new Request("How do I reset my password?"),
            new Request("Angry customer - service is terrible"),
            new Request("Question about billing"),
            new Request("Love this product, keep it up!"),
            new Request("ASAP: Payment processing broken"),
            new Request("Minor issue with settings page"),
            new Request("Excellent customer support team")
        };

        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║    NLP-ENHANCED CHATBOT REQUEST SCHEDULER - PRIORITY QUEUE      ║");
        System.out.println("║         Handling Customer Support Requests Intelligently         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Sort by priority (ascending - 1 is highest)
        Arrays.sort(requests, (a, b) -> Integer.compare(a.priority, b.priority));

        // Display statistics
        displayStatistics(requests);
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    PRIORITIZED REQUEST QUEUE                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Display sorted requests
        int queueNumber = 1;
        for(Request r : requests) {
            String urgencyFlag = r.isUrgent ? " 🔴 URGENT" : "";
            String priorityLabel = getPriorityLabel(r.priority);
            System.out.printf("[%2d] [%s] %s%s%n     ID: %s | Intent: %-13s | Sentiment: %+.2f%n" +
                             "     Message: %s%n%n",
                    queueNumber++, priorityLabel, r.id, urgencyFlag, r.id, r.intent, 
                    r.sentiment, r.message);
        }
    }

    static void displayStatistics(Request[] requests) {
        Map<String, Integer> intentCounts = new HashMap<>();
        Map<String, Integer> priorityCounts = new HashMap<>();
        int urgentCount = 0;
        double avgSentiment = 0;

        for(Request r : requests) {
            intentCounts.put(r.intent, intentCounts.getOrDefault(r.intent, 0) + 1);
            priorityCounts.put(getPriorityLabel(r.priority), priorityCounts.getOrDefault(getPriorityLabel(r.priority), 0) + 1);
            if(r.isUrgent) urgentCount++;
            avgSentiment += r.sentiment;
        }

        avgSentiment /= requests.length;

        System.out.println("📊 REQUEST STATISTICS:");
        System.out.println("────────────────────────────────────────────────────────────────");
        System.out.printf("Total Requests: %d | URGENT: %d%n", requests.length, urgentCount);
        System.out.printf("Average Sentiment: %.2f%n%n", avgSentiment);
        
        System.out.println("📌 By Intent:");
        intentCounts.forEach((intent, count) -> 
            System.out.printf("  • %-15s: %d%n", intent, count)
        );
        
        System.out.println("\n⚡ By Priority:");
        priorityCounts.forEach((priority, count) -> 
            System.out.printf("  • %-15s: %d%n", priority, count)
        );
    }

    static String getPriorityLabel(int priority) {
        switch(priority) {
            case 1: return "CRITICAL";
            case 2: return "HIGH";
            case 3: return "HIGH";
            case 4: return "MEDIUM";
            case 5: return "MEDIUM-LOW";
            case 6: return "LOW";
            case 7: return "LOW";
            default: return "MEDIUM";
        }
    }
}
