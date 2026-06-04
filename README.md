# IskolarZ
### UP Manila BS Computer Science Academic Tracker

[![UP Manila](https://img.shields.io/badge/UP%20Manila-BSCS-7B0000?style=for-the-badge)](https://www.upm.edu.ph/)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android)](https://developer.android.com/)
[![Built with Kotlin & Compose](https://img.shields.io/badge/Built%20with-Kotlin%20%26%20Compose-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![Database](https://img.shields.io/badge/Database-Room%20SQLite-4F5B93?style=for-the-badge&logo=sqlite)](https://developer.android.com/training/data-storage/room)

**IskolarZ** is a dedicated academic tracking utility designed specifically for Bachelor of Science in Computer Science (BSCS) students at the University of the Philippines Manila (UP Manila). Built using Jetpack Compose and modern Android architecture, the application consolidates curriculum checklists and grade records into a simple, offline-first digital tracker.

---

## 1. Core Purpose & Curricular Context

CompSci students at UP Manila manage a rigorous academic structure with strict course progressions, mathematical/statistical series, and specialized tracks. `IskolarZ` provides a reliable platform to monitor checklists, record grades, and forecast academic standing:

*   **Dual-Track Customization:** Organizes specific checklists for the two major specialization pathways within the UPM BSCS program: **Health Informatics (HI)** and **Statistical Computing (SC)**. The application filters checklists and metrics based on the student's selected track.
*   **Sequential Prerequisite & Postrequisite Mapping:** Models critical progression lines directly (e.g., how completing `Math 83` satisfies academic prerequisites for `Math 84` and `Physics 71`). Students can inspect course details to view both required preparatory subjects and forward-acting postrequisites.
*   **UPM Grading Integration:** Calibrated directly for the official UP numeric grading scale (`1.00`, `1.25`, `1.50`, up to `3.00` and `5.00` alongside status flags like `INC` and `DRP`). Calculations factor in course unit weights to provide accurate semestral and cumulative General Weighted Average (GWA) values.

---

## 2. Tab-by-Tab Capabilities

### 📊 Dashboard Console
Serves as the main status screen summarizing the student's current academic standings.
*   **GWA Tracking:** Computes and displays the Cumulative GWA with precision.
*   **Academic Progress Statistics:** Displays total completed credits, remaining target credits, and a progress bar based on the graduation checklist requirements.
*   **History Overview:** Displays a chronological view of the three most recently added grade entries, minimizing screen clutter while providing an intuitive option to review historical grades.

### 📝 Semestral Ledger
A tabular page that organizes grades by academic semesters.
*   **Term Performance Indicators:** Automatically calculates semestral GWA and compiled academic units for any filtered term.
*   **Academic Card Grouping:** Organizes grades using a curated priority order, starting with major Computer Science (`CMSC`) subjects first, followed by math, statistics, and elective major courses, down to General Education (`GE`), Physical Education (`PE`), and `NSTP` requirements.

### 🗺️ Curriculum Checklist Explorer
An interactive digital lookup of the official UP Manila BSCS checklist.
*   **Instant Searching:** Features a search field supporting zero-latency matching by course code (e.g., `CMSC 123`) or descriptive name (e.g., *"Design & Implementation of Programming Languages"*).
*   **Category Sorting:** Categorizes lists easily between *All*, *GE*, *PE*, and *NSTP* courses.
*   **Full Nomenclature:** Renders complete, officially listed subject names and unit weights. Selecting any course opens a card detailing prerequisites, postrequisites, and course outlines.

### 🧮 GWA Target Calculator
A local forecasting utility designed for semester planning.
*   **Score Integration:** Computes grades based on user-entered percentages and customizable component weights (e.g., Exams, Quizzes, Laboratory works).
*   **UP Rating Conversion:** Automatically converts final raw percentage scores to the equivalent UP numeric scale to help students verify what scores they need to achieve their target GWA.

---

## 3. Engineering & Architecture

*   **Clean Design Patterns:** Adheres strictly to the **Model-View-ViewModel (MVVM)** pattern to keep user interfaces decoupled from database and local business processes.
*   **Reactive Flow UI:** Developed entirely in **Jetpack Compose** following Material Design 3 guidelines, employing a clean dark-cyber layout.
*   **Room Database Storage:** Uses **Room SQLite** to store grades locally, ensuring data security and zero offline latency.
*   **Thread Safety:** Manages state across configuration changes using Kotlin `StateFlow` and safe asynchronous lifecycles.
