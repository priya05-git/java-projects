# NLP-Enhanced Chatbot Request Scheduler

## 📌 Problem Statement
A chatbot (like customer support AI) receives thousands of user queries simultaneously.

### ❌ Issues Without Scheduling:
- Some users wait too long for responses
- Urgent queries (e.g., system failures, critical complaints) are delayed
- Less important feedback gets the same priority as critical issues
- No intelligent handling of request volume

### ✅ Solution: Priority Scheduling with NLP
This project implements an **intelligent request scheduler** that automatically classifies and prioritizes customer support queries using Natural Language Processing (NLP).

---

## 🎯 Key Features

### 1. **Automatic Intent Classification**
The system analyzes query text and classifies requests into:
- **COMPLAINT**: Issues, problems, bugs, failures
- **URGENT_QUERY**: Critical, emergency, ASAP requests
- **FEEDBACK**: Suggestions, improvements, positive remarks
- **GENERAL_QUERY**: Standard questions and inquiries

### 2. **Sentiment Analysis**
Evaluates emotional tone of messages:
- **Positive sentiment** (+): Customer is satisfied or praising
- **Negative sentiment** (-): Customer is angry, frustrated, or disappointed
- **Neutral sentiment** (0): Neutral or mixed emotions

### 3. **Urgency Detection**
Identifies critical keywords indicating immediate attention:
- Keywords: `urgent`, `asap`, `critical`, `emergency`, `immediately`, `help please`, `down`, `breaking`

### 4. **Intelligent Priority Assignment**
```
Priority Level              Description
═══════════════════════════════════════════════════════════
1. CRITICAL (🔴)           Urgent + high severity issues
2-3. HIGH                  Complaints & system-affecting issues
4-5. MEDIUM                General queries & moderate issues
6-7. LOW                   Positive feedback & non-urgent requests
```

### 5. **Scalability**
- Handles 15+ requests in demo (scalable to thousands)
- Automatic sorting and queue management
- Unique request IDs for tracking

### 6. **Analytics & Statistics**
- Total request count tracking
- Urgent request count
- Average sentiment across all requests
- Breakdown by intent type
- Distribution across priority levels

---

## 🚀 How to Run

### Prerequisites
- Java 8 or higher
- JDK compiler (`javac`)

### Compilation
```bash
javac ChatbotScheduler.java
```

### Execution
```bash
java ChatbotScheduler
```

### Expected Output
```
╔════════════════════════════════════════════════════════════════╗
║    NLP-ENHANCED CHATBOT REQUEST SCHEDULER - PRIORITY QUEUE      ║
║         Handling Customer Support Requests Intelligently         ║
╚════════════════════════════════════════════════════════════════╝

📊 REQUEST STATISTICS:
────────────────────────────────────────────────────────────────
Total Requests: 15 | URGENT: 3
Average Sentiment: 0.00

📌 By Intent:
  • URGENT_QUERY   : 1
  • COMPLAINT      : 5
  • GENERAL_QUERY  : 5
  • FEEDBACK       : 4

⚡ By Priority:
  • CRITICAL       : 3
  • HIGH           : 3
  • MEDIUM         : 5
  • LOW            : 4

╔════════════════════════════════════════════════════════════════╗
║                    PRIORITIZED REQUEST QUEUE                     ║
╚════════════════════════════════════════════════════════════════╝

[ 1] [CRITICAL] REQ-xxxxxxxx 🔴 URGENT
     ID: REQ-xxxxxxxx | Intent: URGENT_QUERY  | Sentiment: +0.00
     Message: URGENT: System is completely down! Can't access anything!

[ 2] [CRITICAL] REQ-yyyyyyyy 🔴 URGENT
     ...
```

---

## 📊 Understanding the Output

### Request Statistics Section
```
Total Requests: 15           → Total number of incoming requests
URGENT: 3                    → Requests marked as critical/urgent
Average Sentiment: 0.00      → Overall customer sentiment (-1.0 to +1.0)
```

### Request Classification Breakdown
```
By Intent:
  • COMPLAINT      : 5       → System issues and problems
  • GENERAL_QUERY  : 5       → Standard informational requests
  • FEEDBACK       : 4       → Suggestions and positive remarks
  • URGENT_QUERY   : 1       → Time-critical requests
```

### Priority Distribution
```
By Priority:
  • CRITICAL       : 3       → Highest priority (immediate response)
  • HIGH           : 3       → Important (quick response)
  • MEDIUM         : 5       → Standard (normal response)
  • LOW            : 4       → Lower priority (can wait)
```

### Queue Display Format
```
[Queue Position] [Priority Level] REQ-ID [Urgency Flag]
     Request Metadata
     Full Message Content
```

**Example:**
```
[ 1] [CRITICAL] REQ-abc123def 🔴 URGENT
     ID: REQ-abc123def | Intent: COMPLAINT | Sentiment: -0.25
     Message: ASAP: Payment processing broken
```

---

## 🔧 Core Components

### Request Class
- **Attributes**:
  - `message`: Original customer message
  - `intent`: Classified intent type
  - `sentiment`: Sentiment score (-1.0 to +1.0)
  - `priority`: Numeric priority (1=highest)
  - `isUrgent`: Boolean flag for urgent requests
  - `id`: Unique request identifier
  - `timestamp`: Request creation time

- **Methods**:
  - `classifyIntent()`: NLP-based intent detection
  - `analyzeSentiment()`: Sentiment scoring algorithm
  - `detectUrgency()`: Urgency keyword identification
  - `calculatePriority()`: Priority calculation logic

### Main Scheduler
- Loads sample requests (demo uses 15 requests)
- Automatically sorts by priority
- Displays statistics and analytics
- Outputs prioritized queue for processing

---

## 📈 Real-World Application

### Scenario: Customer Support Center
```
Incoming Requests (Random Order):
├── "Love your product!" → LOW priority
├── "System down URGENT!" → CRITICAL priority
├── "How do I login?" → MEDIUM priority
├── "Payment broken, ASAP!" → CRITICAL priority
└── "Good suggestions" → LOW priority

After NLP Processing & Priority Scheduling:
├── CRITICAL: "System down URGENT!" → Process immediately
├── CRITICAL: "Payment broken, ASAP!" → Process immediately
├── MEDIUM: "How do I login?" → Process in queue
└── LOW: "Love your product!" → Process when available
└── LOW: "Good suggestions" → Process later
```

### Benefits
✅ Urgent issues resolved first → Reduced customer frustration  
✅ No critical issues slip through → Better reliability  
✅ Automatic classification → Reduced manual overhead  
✅ Sentiment tracking → Identify angry customers  
✅ Analytics → Understand support needs  

---

## 💡 Algorithm Details

### Intent Classification
Uses keyword matching to identify query type:
```
If message contains: "complaint", "problem", "issue", "broken", "error", "failed"
→ Intent = COMPLAINT (Priority: HIGH)

If message contains: "feedback", "suggest", "improve", "good", "excellent", "feature request"
→ Intent = FEEDBACK (Priority: LOW)

If message contains: "urgent", "asap", "critical", "emergency"
→ Intent = URGENT_QUERY (Priority: CRITICAL)

Otherwise → Intent = GENERAL_QUERY (Priority: MEDIUM)
```

### Sentiment Scoring
Counts positive and negative words:
```
Positive words: +0.25 each
Negative words: -0.25 each
Range: -1.0 (very negative) to +1.0 (very positive)
```

### Priority Calculation
```
if (isUrgent) → Priority = 1 (CRITICAL)
else if (intent == COMPLAINT) → Priority = 2 (HIGH)
else if (intent == URGENT_QUERY) → Priority = 3 (HIGH)
else if (sentiment < -0.5) → Priority = 4 (MEDIUM)
else if (intent == GENERAL_QUERY) → Priority = 5 (MEDIUM-LOW)
else if (sentiment > 0.3) → Priority = 7 (LOW)
else → Priority = 6 (DEFAULT)
```

---

## 🎓 Learning Outcomes

This project demonstrates:
1. **NLP Basics**: Text classification and sentiment analysis
2. **Data Structures**: Arrays and sorting algorithms
3. **OOP Concepts**: Classes, static methods, encapsulation
4. **Algorithms**: Priority scheduling and sorting
5. **Software Design**: Real-world problem solving

---

## 📝 Sample Test Cases

### Test 1: Urgent System Issue
**Input:** "URGENT: System is completely down! Can't access anything!"  
**Classification:**
- Intent: URGENT_QUERY ✓
- Sentiment: -0.00
- Urgency: YES (contains "URGENT")
- **Priority: CRITICAL (1)** ✓

### Test 2: Negative Complaint
**Input:** "Complaint about slow response times"  
**Classification:**
- Intent: COMPLAINT ✓
- Sentiment: -0.00
- Urgency: NO
- **Priority: HIGH (2)** ✓

### Test 3: Positive Feedback
**Input:** "Love your new UI design"  
**Classification:**
- Intent: FEEDBACK ✓
- Sentiment: +0.25 (positive)
- Urgency: NO
- **Priority: LOW (7)** ✓

### Test 4: General Question
**Input:** "How do I reset my password?"  
**Classification:**
- Intent: GENERAL_QUERY ✓
- Sentiment: 0.00
- Urgency: NO
- **Priority: MEDIUM-LOW (5)** ✓

---

## 🔄 Future Enhancements

- [ ] Machine Learning model for better NLP accuracy
- [ ] Multi-language support (language detection)
- [ ] Customer history tracking (VIP customers)
- [ ] Response time SLA enforcement
- [ ] Integration with ticketing systems
- [ ] Real-time queue monitoring dashboard
- [ ] A/B testing for priority algorithms
- [ ] Escalation rules for stalled requests
- [ ] Email/SMS notifications for urgent cases
- [ ] AI chatbot response generation

---

## 📄 License
Open source - Feel free to modify and extend for your needs

## 👨‍💻 Author
NLP Chatbot Scheduler Project

## 🤝 Contributing
Suggestions and improvements welcome!

---

## ❓ FAQ

**Q: How does it handle thousands of requests?**  
A: The sorting algorithm runs in O(n²) time. For production, use faster sorting (Arrays.sort → O(n log n)).

**Q: Can I add custom keywords?**  
A: Yes! Modify the keyword arrays in `classifyIntent()` and `detectUrgency()` methods.

**Q: How accurate is sentiment analysis?**  
A: This is basic lexicon-based sentiment. For better accuracy, use ML models like BERT or VADER.

**Q: Can this handle emojis or URLs?**  
A: Currently handles text. To add media support, extend the analysis methods.

**Q: How do I integrate with a real chatbot?**  
A: This is the scheduling layer. Connect it to your chatbot API's message queue system.

---

**Last Updated:** April 11, 2026  
**Version:** 2.0 (NLP-Enhanced with Advanced Scheduling)
