package com.example.ui

enum class AppLanguage {
    EN, ZH
}

object Translation {
    fun get(lang: AppLanguage, key: String): String {
        val en = mapOf(
            // Top Bar
            "app_title" to "AI FILE DASHBOARD",
            "app_subtitle" to "INTELLIGENT UNDERSTANDING SYSTEM",
            "tab_mainframe" to "Mainframe",
            "tab_cognitive" to "Cognitive Map",
            "tab_rule" to "Rule Deck",

            // Settings Dialog
            "settings_title" to "SYSTEM CONFIGURATION",
            "settings_lang_label" to "UI Language (系統語言)",
            "settings_lang_en" to "English (US)",
            "settings_lang_zh" to "繁體中文 (Chinese)",
            "settings_theme_label" to "Visual Core Theme",
            "settings_theme_val" to "Cyberpunk Dark Core (Active)",
            "settings_close" to "DISMISS",

            // AIBanner
            "ai_core_mainframe" to "AI CORE MAINFRAME",
            "system_active" to "SYSTEM ACTIVE",
            "scanned_sectors" to "SCANNED SECTORS",
            "total_volume" to "TOTAL VOLUME",
            "ai_accuracy" to "AI ACCURACY",

            // ScanConsoleCard
            "terminal_scan_logs" to "TERMINAL SCAN LOGS",
            "sector_progress" to "Sector Progress: %s / %s files analysed.",
            "idle" to "Idle / System awaiting sectors.",

            // ActionsTerminal
            "scan_directory" to "SCAN DIRECTORY",
            "demo_corpus" to "DEMO CORPUS",

            // StorageAnalysisCard
            "storage_matrix" to "VOLUME & STORAGE MATRIX",
            "docs" to "Documents",
            "visual" to "Visual Images",
            "media" to "Media Videos",
            "apks" to "System APKs",
            "audio" to "Audio Streams",
            "archives" to "Archives / Zip",
            "unknown_category" to "Unknown / Cyber",

            // DistributionChartsRow
            "sector_type_dist" to "SECTOR TYPE DISTRIBUTION",
            "semantic_knowledge" to "SEMANTIC KNOWLEDGE MAP",
            "primary" to "Primary: %s",
            "dominant" to "Dominant: %s",
            "empty_telemetry" to "Telemetry Empty",

            // DuplicatesAlertCard
            "redundancy_logs" to "REDUNDANCY / DUPLICATE SYSTEM LOGS",
            "redundancy_desc" to "Detected %s identical files spanning multiple directories. Tap to investigate and purge storage.",

            // RecentActivityFeed
            "recent_sectors" to "REAL-TIME SECTOR CHRONOLOGY",
            "no_recent_scans" to "Mainframe inactive. No recent sector scans found.",

            // KnowledgeMapScreen
            "semantic_matrix_title" to "AI SEMANTIC MATRIX MAP",
            "semantic_matrix_desc" to "Double-tap or select sectors to decrypt local knowledge databases.",
            "sector_study" to "學習資料與開發專案區",
            "sector_study_sub" to "Study Materials & Software Projects",
            "sector_gaming" to "遊戲存檔與畫面截圖區",
            "sector_gaming_sub" to "Game Databases & Media Records",
            "sector_work" to "工作文件與商業報表區",
            "sector_work_sub" to "Corporate Worksheets & Invoices",
            "sector_junk" to "系統垃圾與快取暫存區",
            "sector_junk_sub" to "Redundant Registers & Temp Cache",
            "sector_other" to "其他雜項與外部資源區",
            "sector_other_sub" to "General Archives & Custom Sectors",
            "sector_offline" to "Sector offline. No files registered.",

            // FileDetailsDialog
            "sector_telemetry" to "SECTOR TELEMETRY",
            "file_size" to "File Size",
            "mime_type" to "MIME Type",
            "registry_date" to "Registry Date",
            "ai_semantic_inference" to "🧠 AI SEMANTIC INFERENCE",
            "confidence" to "CONFIDENCE %s%%",
            "calibrated_watermark" to "MANUALLY CALIBRATED SYSTEM VALUE",
            "guess_breakdown" to "🎯 UNKNOWN FILE AI GUESS BREAKDOWN",
            "calibrate_core" to "CALIBRATE AI CORE VALUES",
            "base_type_label" to "Base Type:",
            "semantic_class_label" to "Semantic Class:",
            "predicted_use" to "Predicted Use/Purpose:",
            "teach_ai_rule" to "Teach AI rule for future file scans",
            "match_name" to "Match Name",
            "match_ext" to "Match Extension",
            "apply_calibration" to "APPLY MAIN CALIBRATION",
            "cancel" to "CANCEL",
            "purge" to "PURGE",
            "calibrate" to "CALIBRATE",

            // FeedbackRulesScreen
            "ai_rules_title" to "AI COGNITIVE LEARNED RULES",
            "ai_rules_desc" to "System rules generated from user feedback calibration. Future scans matching these patterns are updated automatically.",
            "no_rules_title" to "NO COGNITIVE RULES CONFIGURED",
            "no_rules_desc" to "Calibrate file categories in the detail dialog and select 'Teach AI rule' to inject custom learning logic here.",
            "if_clause" to "IF ",
            "ends_in" to "file ends in .%s",
            "contains" to "filename contains '%s'",
            "then_clause" to "THEN AUTO-CLASSIFY AS:",
            "purpose" to "Purpose: %s"
        )

        val zh = mapOf(
            // Top Bar
            "app_title" to "AI 檔案儀表板",
            "app_subtitle" to "智慧檔案理解與分析系統",
            "tab_mainframe" to "控制中心",
            "tab_cognitive" to "知識地圖",
            "tab_rule" to "規則集",

            // Settings Dialog
            "settings_title" to "系統核心設定",
            "settings_lang_label" to "介面語言 (UI Language)",
            "settings_lang_en" to "English (US)",
            "settings_lang_zh" to "繁體中文 (Chinese)",
            "settings_theme_label" to "視覺主題核心",
            "settings_theme_val" to "霓虹深色科技感 (作用中)",
            "settings_close" to "關閉設定",

            // AIBanner
            "ai_core_mainframe" to "AI 主機核心狀態",
            "system_active" to "系統運作中",
            "scanned_sectors" to "已掃描檔案數",
            "total_volume" to "總儲存容量",
            "ai_accuracy" to "AI 平均信心度",

            // ScanConsoleCard
            "terminal_scan_logs" to "終端掃描即時日誌",
            "sector_progress" to "磁區掃描進度: %s / %s 檔案分析中。",
            "idle" to "系統就緒 / 靜待掃描指令。",

            // ActionsTerminal
            "scan_directory" to "掃描目錄",
            "demo_corpus" to "載入測試資料",

            // StorageAnalysisCard
            "storage_matrix" to "檔案容量與儲存矩陣",
            "docs" to "文件檔案",
            "visual" to "視覺圖像",
            "media" to "影音視訊",
            "apks" to "應用安裝包 (APK)",
            "audio" to "音訊串流",
            "archives" to "壓縮封包 (Zip)",
            "unknown_category" to "未知 / 混合檔案",

            // DistributionChartsRow
            "sector_type_dist" to "儲存類型分佈",
            "semantic_knowledge" to "語意理解知識地圖",
            "primary" to "主要類型: %s",
            "dominant" to "主要語意: %s",
            "empty_telemetry" to "暫無分析數據",

            // DuplicatesAlertCard
            "redundancy_logs" to "重複檔案與冗餘日誌警告",
            "redundancy_desc" to "偵測到 %s 個相同的檔案存在於不同路徑。點擊進行深度分析並釋放空間。",

            // RecentActivityFeed
            "recent_sectors" to "即時磁區掃描日誌",
            "no_recent_scans" to "主機閒置中。尚未有最近的掃描紀錄。",

            // KnowledgeMapScreen
            "semantic_matrix_title" to "AI 語意矩陣地圖",
            "semantic_matrix_desc" to "點選或展開各磁區，解密並探索本地檔案知識庫。",
            "sector_study" to "學習資料與開發專案區",
            "sector_study_sub" to "學習教材、學術筆記與原始碼專案",
            "sector_gaming" to "遊戲存檔與畫面截圖區",
            "sector_gaming_sub" to "遊戲存檔、Minecraft 紀錄與媒體截圖",
            "sector_work" to "工作文件與商業報表區",
            "sector_work_sub" to "商業簡報、電子發票與專案試算表",
            "sector_junk" to "系統垃圾與快取暫存區",
            "sector_junk_sub" to "日誌輸出、快取、臨時檔與冗餘暫存",
            "sector_other" to "其他雜項與外部資源區",
            "sector_other_sub" to "通用壓縮檔、未知類型與自訂檔案",
            "sector_offline" to "此磁區尚無已分析的檔案。",

            // FileDetailsDialog
            "sector_telemetry" to "檔案遙測詳情",
            "file_size" to "檔案大小",
            "mime_type" to "MIME 類型",
            "registry_date" to "登記時間",
            "ai_semantic_inference" to "🧠 AI 語意理解與推論",
            "confidence" to "AI 信心分數 %s%%",
            "calibrated_watermark" to "使用者已手動校正系統數值",
            "guess_breakdown" to "🎯 未知檔案 AI 分類預測概率",
            "calibrate_core" to "校正 AI 核心分類",
            "base_type_label" to "基礎類型：",
            "semantic_class_label" to "語意類別：",
            "predicted_use" to "AI 推測用途：",
            "teach_ai_rule" to "記住此次校正，下次自動套用此規則",
            "match_name" to "比對檔名",
            "match_ext" to "比對副檔名",
            "apply_calibration" to "套用校正變更",
            "cancel" to "取消",
            "purge" to "移除紀錄",
            "calibrate" to "AI 校正",

            // FeedbackRulesScreen
            "ai_rules_title" to "AI 認知學習規則集",
            "ai_rules_desc" to "系統根據您的校正歷史所自動學習到的規則。未來掃描時若符合條件，將自動套用。",
            "no_rules_title" to "尚未建立任何認知規則",
            "no_rules_desc" to "在檔案詳情視窗中，點選「AI 校正」並勾選「記住此次校正」，系統就會自動記錄您的喜好並呈現在這裡。",
            "if_clause" to "若 ",
            "ends_in" to "副檔名為 .%s",
            "contains" to "檔案名稱包含 '%s'",
            "then_clause" to "則自動分類為：",
            "purpose" to "推測用途: %s"
        )

        return if (lang == AppLanguage.ZH) {
            zh[key] ?: en[key] ?: key
        } else {
            en[key] ?: key
        }
    }
}
