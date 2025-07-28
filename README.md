# ShellGuard: AI-Assisted Command Approval System
**Secure. Auditable. AI-Powered.**

## 🛡️ Overview
ShellGuard is an **AI-assisted command approval and risk assessment system** for SSH environments. It adds a **human-in-the-loop workflow** for approving or rejecting commands, reducing operational risk and providing a full audit trail.

**Key goals:**
- Prevent high-risk commands from being executed without review
- Integrate AI for adaptive risk scoring
- Enable compliance with auditable approvals

---

## ✨ Features
✔ **AI Risk Assessment** – Score commands before execution  
✔ **Human Approval Workflow** – Approve/reject high-risk commands  
✔ **Pluggable Architecture** – Add your own assessors & approvers  
✔ **Real-Time Web UI** – Built with HTMX + Tailwind  
✔ **Kotlin + Spring Boot Backend** – Reliable and scalable foundation  
✔ **WebSocket-driven Live Updates**
✔ **AI Risk Assessment** – Score commands before execution using pluggable assessors (e.g., rule-based, composite, auto-approve, failsafe modes)
✔ **RKCL (RoboKeys Control Language)** – Support for high-level command types like `TEXT` (raw text), `LINE` (text + Enter), `KEY` (special keys like Enter, Tab, Arrows), `COMBO` (key combinations like Ctrl+C), and `EDIT` (cut, copy, paste).


---

## 🚀 Quick Start
The application will start on http://localhost:8080 by default.

Important Notes for Quick Start:

A stub SSH server is enabled by default for testing, allowing you to connect locally using demo/demo credentials. This is for development and testing ONLY.

### Accessing Dashboards and APIs
Once running, open these in your browser:

Main Landing Page: http://localhost:8080/

AI Command Approval Center: http://localhost:8080/approval-dashboard.html

Terminal Test Client: http://localhost:8080/terminal-test-client.html

Admin Dashboard: http://localhost:8080/admin-dashboard.html

Debug Dashboard: http://localhost:8080/debug-dashboard.html

WebSocket API Documentation: http://localhost:8080/api-docs.html


### Prerequisites
- **Java 21+**
- **Gradle 8+**
- (Optional) **Docker** for containerized deployment

### Clone & Run
```
git clone https://github.com/robokeys/shellguard.git
cd shellguard
./gradlew bootRun
```

## 🖥️ How It Works -  Workflow:

    SSH Command Received → Intercepted by ShellGuard

    Risk Assessment → AI or rule-based engine scores it

        Low risk → Auto-approved

        High risk → Sent to Approval Center for review

    Execute or Reject → Safe, auditable execution

## 🔌 Architecture

    Backend: Kotlin + Spring Boot

    UI: HTMX + CSS

    Communication: WebSockets for real-time approval updates

    Workflow Engine: Event-driven, pluggable assessors & approvers


