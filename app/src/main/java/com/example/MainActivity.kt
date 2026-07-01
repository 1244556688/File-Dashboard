package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.FileRepository
import com.example.data.ScannedFile
import com.example.ui.*
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create Database and Repository instances
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FileRepository(database)

        setContent {
            MyApplicationTheme {
                // Initialize Viewmodel via Factory
                val mainViewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(repository)
                )

                val context = LocalContext.current

                // Load language preferences once on start
                LaunchedEffect(Unit) {
                    mainViewModel.loadLanguage(context)
                }

                // Retrieve State
                val scannedFiles by mainViewModel.scannedFiles.collectAsStateWithLifecycle()
                val feedbackRules by mainViewModel.feedbackRules.collectAsStateWithLifecycle()
                val isScanning by mainViewModel.isScanning.collectAsStateWithLifecycle()
                val scanProgress by mainViewModel.scanProgress.collectAsStateWithLifecycle()
                val scanTotal by mainViewModel.scanTotal.collectAsStateWithLifecycle()
                val scanResult by mainViewModel.scanResult.collectAsStateWithLifecycle()
                val appLanguage by mainViewModel.appLanguage.collectAsStateWithLifecycle()

                // State for active screen navigation
                var selectedTabIndex by remember { mutableStateOf(0) }

                // State for settings and active dialog detail
                var showSettingsDialog by remember { mutableStateOf(false) }
                var selectedFileForDetail by remember { mutableStateOf<ScannedFile?>(null) }

                // Launcher for Storage Access Framework open tree selector
                val dirPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocumentTree()
                ) { uri ->
                    if (uri != null) {
                        // Persist URI permissions to read offline later
                        try {
                            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                        } catch (e: Exception) {
                            // Ignored if permissions already taken or unsupported in simulated shell
                        }
                        mainViewModel.scanDirectory(context, uri)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        CenterAlignedTopAppBar(
                            title = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = Translation.get(appLanguage, "app_title"),
                                        color = CyberCyan,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 2.sp
                                    )
                                    Text(
                                        text = Translation.get(appLanguage, "app_subtitle"),
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { showSettingsDialog = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "System Configuration Settings",
                                        tint = CyberCyan
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = CyberDark
                            ),
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(0.dp)
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CyberDark,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .navigationBarsPadding()
                                .border(1.dp, Color(0xFF1E293B))
                        ) {
                            NavigationBarItem(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                icon = { Icon(imageVector = Icons.Default.Grid3x3, contentDescription = "Dashboard") },
                                label = { Text(Translation.get(appLanguage, "tab_mainframe"), fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color(0xFF152A3F)
                                )
                            )

                            NavigationBarItem(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Knowledge Map") },
                                label = { Text(Translation.get(appLanguage, "tab_cognitive"), fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color(0xFF152A3F)
                                )
                            )

                            NavigationBarItem(
                                selected = selectedTabIndex == 2,
                                onClick = { selectedTabIndex = 2 },
                                icon = { Icon(imageVector = Icons.Default.SettingsSuggest, contentDescription = "Calibration") },
                                label = { Text(Translation.get(appLanguage, "tab_rule"), fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyberCyan,
                                    selectedTextColor = CyberCyan,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color(0xFF152A3F)
                                )
                            )
                        }
                    },
                    containerColor = CyberDark
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Display correct subscreen
                        when (selectedTabIndex) {
                            0 -> DashboardScreen(
                                files = scannedFiles,
                                isScanning = isScanning,
                                progress = scanProgress,
                                total = scanTotal,
                                scanResult = scanResult,
                                appLanguage = appLanguage,
                                onScanClick = { dirPickerLauncher.launch(null) },
                                onDemoClick = { mainViewModel.generateDemoCorpus() },
                                onClearClick = { mainViewModel.clearAllScans() },
                                onFileClick = { selectedFileForDetail = it }
                            )
                            1 -> KnowledgeMapScreen(
                                files = scannedFiles,
                                appLanguage = appLanguage,
                                onFileClick = { selectedFileForDetail = it }
                            )
                            2 -> FeedbackRulesScreen(
                                rules = feedbackRules,
                                appLanguage = appLanguage,
                                onDeleteRule = { mainViewModel.deleteRule(it) }
                            )
                        }

                        // Display Settings Dialog if opened
                        if (showSettingsDialog) {
                            SettingsDialog(
                                currentLanguage = appLanguage,
                                onLanguageChange = { mainViewModel.setLanguage(context, it) },
                                onDismiss = { showSettingsDialog = false }
                            )
                        }

                        // Display File Detail & Calibration Dialog if selected
                        selectedFileForDetail?.let { file ->
                            FileDetailsDialog(
                                file = file,
                                appLanguage = appLanguage,
                                onDismiss = { selectedFileForDetail = null },
                                onSubmitFeedback = { targetFile, newBase, newSemantic, newPurpose, learnRule, ruleType ->
                                    mainViewModel.submitFeedback(
                                        file = targetFile,
                                        newBaseType = newBase,
                                        newSemanticClass = newSemantic,
                                        newAiPurpose = newPurpose,
                                        learnAsRule = learnRule,
                                        ruleType = ruleType
                                    )
                                    selectedFileForDetail = null
                                },
                                onDeleteRecord = { mainViewModel.deleteFileRecord(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
