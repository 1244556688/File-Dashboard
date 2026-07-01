# AI File Dashboard – Intelligent Understanding System

An advanced, cybernetic-themed Android file cognitive system built with Jetpack Compose, Kotlin Coroutines, and local Room persistence. 

This is not a traditional file manager designed to sort extensions; instead, it is an **Intelligent Understanding System** that uses semantic heuristics and the Google Gemini API to analyze, categorize, and synthesize local user assets into a visual knowledge mainframe.

---

## 🧠 Core Architecture & Features

### 1. Multi-Tier AI Understanding Engine
The system analyzes files by assessing their semantic footprint (filename context, path patterns, size parameters, and system descriptors) rather than relying strictly on file extensions:
*   📂 **Base Categorization**: Detects core media and system wrappers (Images, Videos, Documents, APKs, Audio, Archives, and Custom Binary Streams).
*   🧠 **Semantic Knowledge Mainframe**: Discovers cognitive purposes and maps files into dedicated folders (`STUDY` / `WORK` / `GAMING` / `SCREENSHOT` / `CODE` / `JUNK` / `OTHER`).
*   🤖 **Purpose Synthesizer**: Generates dynamic descriptions explaining what the file actually represents (e.g., *“Astronomy lecture notes on astrophysics”*, *“Minecraft World Backup state”*).
*   📈 **Confidence Scoring**: Computes accuracy indexes (0–100%) for all classifications.

### 2. Unknown File Heuristics
Unrecognized file structures (`.xyz`, `.dat`, `.bin`, or files with missing extensions) are analyzed dynamically:
*   Does not group them under generic "unknown" buckets.
*   Runs speculative prediction models producing a split probability list (e.g., *“Developer Backup: 65%, Custom Archive: 25%, Binary Stream: 10%”*).

### 3. Cognitive User Learning (Learnable AI)
The dashboard features an integrated human-in-the-loop calibration module:
*   Allows manual corrections of any file's base category, semantic group, or description.
*   Learns user calibration actions locally to create system rules (e.g., *“All files containing 'assignment' should map to STUDY”* or *“All .xyz files are Software Backups”*).
*   Subsequent scanner sweeps apply learned offline rules automatically with 100% precision.

### 4. Hacker Command Dashboard (Visual Mainframe)
*   **Holographic Space Matrix**: Real-time progress indicators representing volume consumption across categories.
*   **Pie Telemetry Charts**: Custom Compose Canvas rings mapping type distributions.
*   **Cognitive Map Nodes**: Animated, expanding sector decks for exploring Work, Study, Gaming, and Junk.
*   **Redundancy Scanner**: Detects duplicate files across separate paths to free up storage.
*   **Terminal Log Feed**: Live interactive readout tracking SAF (Storage Access Framework) filesystem sweeps.

---

## 📥 Local Installation & Build Instructions

Follow these steps to deploy and build the AI File Dashboard inside Android Studio:

### ⚙️ System Requirements
*   **IDE**: Android Studio Ladybug (2024.2.1+) or newer
*   **Kotlin SDK**: v2.2.10
*   **Android SDK Compatibility**: Compile SDK 36, Target SDK 36, Min SDK 24 (Compatible with Android 7.0+)
*   **Gradle Build System**: Gradle v8.x + Kotlin DSL (.kts)
*   **KSP Plugin**: Kotlin Symbol Processing for Room compilation

### 🛠️ Setting Up from Source
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/your-repo/ai-file-dashboard.git
    cd ai-file-dashboard
    ```
2.  **Open in Android Studio**:
    *   Select **File > Open** and choose the root `ai-file-dashboard` folder.
    *   Let Android Studio initialize, download dependencies, and index the workspace.
3.  **Sync Gradle Project**:
    *   Click **Sync Project with Gradle Files** in the toolbar.
4.  **Configure API Keys**:
    *   Input your Gemini API key in the AI Studio **Secrets panel**, or copy `.env.example` to `.env` and fill in `GEMINI_API_KEY`:
    ```env
    GEMINI_API_KEY=YOUR_ACTUAL_GEMINI_API_KEY
    ```
5.  **Compile & Run**:
    *   Select your physical device or emulator.
    *   Click **Run 'app'** (`Shift + F10`).

---

## 📦 Compiling and Exporting the APK

To build a standalone installable release or debug APK:

### 1. Build via Android Studio
1.  Navigate to the top menu and select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2.  Android Studio will execute the Gradle compile pipeline.
3.  Once compilation finishes, a popup will appear at the bottom right. Click **Locate** to retrieve the file.
4.  The generated APK is located in:
    `app/build/outputs/apk/debug/app-debug.apk`

### 2. Build via Command Line
Execute this Gradle task in the terminal of the project's root directory:
```bash
gradle assembleDebug
```
The installable APK will be exported to:
`app/build/outputs/apk/debug/app-debug.apk`

### 3. Installing on Android Devices
1.  Transfer the `app-debug.apk` to your phone's local storage.
2.  Open your preferred file browser on the device and select the APK.
3.  If prompted, enable **Allow installation from unknown sources** in your device security settings.
4.  Click **Install** and launch the application!

---

## 🧠 AI Module & Logical Design

### Semantic Analysis Architecture
The app features a unified gateway pattern in `GeminiClassifier` that manages two distinct categorizers:
1.  **Gemini REST Engine**: Constructed with standard Retrofit and OkHttp clients, using a custom 60-second connection timeout optimized for deep LLM evaluation. It generates structured JSON outputs via custom instructions, ensuring reliable API decoding.
2.  **Offline Heuristics Engine**: Operates locally using keyword mapping and regex indicators. If the user is offline or hasn't supplied a `GEMINI_API_KEY`, the heuristics engine takes over immediately, providing robust classifications and speculative probabilities for unknown files.

### LLM Prompt Architecture
```json
{
  "baseType": "IMAGE" | "VIDEO" | "DOCUMENT" | "APK" | "AUDIO" | "ARCHIVE" | "UNKNOWN",
  "semanticClass": "STUDY" | "WORK" | "GAMING" | "SCREENSHOT" | "CODE" | "JUNK" | "OTHER",
  "aiPurpose": "Short 3-8 word description of inferred purpose",
  "confidence": 0-100,
  "probabilities": "Breakdown guesses of file categories (Only if UNKNOWN)"
}
```

---

## 🚀 Future Roadmap

*   **Multimodal File Contents Scanner**: Expand semantic analysis by sending the actual raw headers or text bytes of documents directly to Gemini to read and understand deep file contents.
*   **Decentralized Sync**: Sync knowledge databases across multiple Android devices securely.
*   **Vector Database Search**: Add local Vector Search and Embeddings using `gemini-embedding-2-preview` to perform natural language semantic queries on local files.
*   **Personal Digital Brain (PDB)**: Expand the platform into a digital second-brain companion that answers deep contextual questions about all your saved research, work invoices, and notes.

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).
