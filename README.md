# IskolarZ: The Comprehensive UP Manila BS Computer Science Academic Tracker & Copilot

[![UP Manila Colegial](https://img.shields.io/badge/UP%20Manila-BSCS-7B0000?style=for-the-badge)](https://www.upm.edu.ph/)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Built with Kotlin & Compose](https://img.shields.io/badge/Built%20with-Kotlin%20%26%20Compose-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Database](https://img.shields.io/badge/Database-Room%20SQLite-4F5B93?style=for-the-badge&logo=sqlite)](https://developer.android.com/training/data-storage/room)

**IskolarZ** is a state-of-the-art academic companion app designed specifically for Bachelor of Science in Computer Science (BSCS) students at the University of the Philippines Manila (UP Manila). Built atop Jetpack Compose and modern Android architecture, the application converts complex curriculum configurations and grade management pipelines into an intuitive, offline-first digital copilot.

---

## 1. Value Proposition & Curricular Context

Compsci students at UP Manila operate under a highly demanding academic framework. Efficient tracking of course completions, compliance with strict academic standings for scholarships or Latin honors, and mapping prerequisites represent significant administrative friction. `IskolarZ` is built specifically to address these challenges with direct, structural knowledge of the UPM BSCS system:

*   **Adaptive Dual-Track Support:** The UPM BSCS curriculum splits into two specialization pathways: **Health Informatics (HI)** (combining computing with medical informatics, clinical frameworks, and biochemistry) and **Statistical Computing (SC)** (focusing on mathematical modeling, advanced probabilities, and data science). The application automatically configures checklist tracks and metric goals based on the student's selected branch.
*   **Prerequisite and Postrequisite Analysis:** A single delayed class can disrupt several semesters of registration due to chain dependencies. Each curriculum item features native mapping of required preparatory courses alongside forward-looking postrequisites (e.g., how `Math 55` unlocks downstream computing theory).
*   **UPM Numeric Grading System Compatibility:** The app bypasses generic Letter or GPA scales in favor of the official University of the Philippines scale (`1.00` to `3.00` in `0.25` steps, with non-numeric flags like `INC` and `DRP`). GWAs are computed utilizing weighted credit hours to guarantee administrative conformity.

---

## 2. Platform Modules & Functional Depth

### 🚀 Onboarding Hub & Setup
Upon launch, students are greeted with a customized cyber-themed terminal landing page.
*   **Dynamic Identity Capture:** Customization of individual nickname aliases and academic start periods with computed dropdown semester ranges extending from Academic Year 2022 to 2030.
*   **Specialization Alignment:** Core toggle interfaces styled with modern, high-contrast, context-aware iconography mapping Health Informatics (Clinical Cross) and Statistical Computing (Data Analytics) options.

---

### 📊 The Dashboard Console
The primary viewport aggregates metrics and registers the student's overall progress.
*   **Cum GWA Gauge:** Computes and displays the Cumulative General Weighted Average with four decimal places of precision, honoring UPM registrar standards.
*   **GWA Privacy Shield:** A single-tap toggle allows students to obscure their grades and GWA instantly behind a secure mask when viewing in public locations.
*   **Latin Honors Predictor:** Evaluates GWA thresholds and credit status continuously to surface real-time standing evaluations (e.g., *Summa Cum Laude*, *Magna Cum Laude*, *Cum Laude*).
*   **Progression Metrics:** Displays Total Target Units (158 units), Earned Credits, and Remaining Credits, combined with a linear progress bar reflecting overall graduation progress.
*   **Grade Records Log:** Shows the three most recent evaluations, allowing students to check entries at a glance. Includes an accessible dialogue to view and search full archives.

---

### 📝 Semestral Ledger & Grade Manager
This container manages historic evaluations, organized by performance periods.
*   **Period Filtering:** Allows students to isolate semesters through dropdown interfaces to check chronological records.
*   **Semestral Key Performance Indicators:** Dynamically calculates metrics for isolated semesters, including term GPA, completed academic unit count, and non-GE specific requirements like PE/NSTP credits.
*   **Smart Priority Sorting:** To prevent log clutter, courses within the semestral cards are prioritized using a strict academic hierarchy:
    1.  **Computer Science (CMSC)** major lines.
    2.  **Associated Core Majors** (Mathematics, Statistics, Physics, Chemistry, Health Informatics).
    3.  **General Education (GE)** requirements.
    4.  **Physical Education (PE)** courses.
    5.  **National Service Training Program (NSTP)** courses.

---

### 🗺️ Curriculum checklist Navigator
An interactive, searchable digital translation of the official University of the Philippines Manila BS CS checklist.
*   **Dynamic Query Search:** Instant searching matching course codes (e.g., `CMSC 137`, `Math 83`) or descriptive name blocks (e.g., *"Design and Analysis of Algorithms"*).
*   **Search Categories:** Filters to divide categories: *All Checklist*, *GE Courses*, *PE Courses*, and *NSTP Courses*.
*   **Dynamic Curricular Slots:** General placeholders (e.g., generic General Education or PE slots) automatically update when matching subjects are registered, displaying course titles, exact weight, and current ratings in place of placeholders.
*   **Detailed Class Drawers:** Selecting any item exposes a nested drawer holding full course names, target credits, strict prerequisite rules, and postrequisites.

---

### 🧮 Weighted assessments Scratchpad
A local tool designed for predictive academic planning.
*   **Assessment Modeling:** Create subject calculation sheets grouping dynamic coursework categories (e.g. Quizzes, Written Exams, Special Laboratory Projects).
*   **Weight & Score Reciprocity:** Evaluates score items against their respective weights, updating current performance metrics.
*   **Scale Conversion:** Converts computed raw percentage scores to the equivalent UPM scalar grade, informing users of required scores to secure target marks.

---

## 3. Engineering & Architectural Design

The codebase relies on modern software design patterns to ensure scalability, offline reliability, and rendering speed:

*   **Pattern Standard:** Uses classic **MVVM (Model-View-ViewModel)**. The presentation layer remains cleanly decoupled from local data logic, driving rendering through reactive streams.
*   **Reactive UI:** Built entirely in **Jetpack Compose**, implementing Material Design 3 guidelines, dynamic color pairings, responsive layouts, and edge-to-edge support.
*   **Local SQLite Storage:** Uses **Room Database** with Kotlin Coroutines and Flows to allow real-time database updates and fluid UI transitions.
*   **State Safety:** Employs `StateFlow` and `collectAsStateWithLifecycle()` to manage orientation shifts and application updates while maintaining state integrity.

---

*IskolarZ combines elegant design with academic tracking to support UP Manila Computer Science students throughout their degree program.*
