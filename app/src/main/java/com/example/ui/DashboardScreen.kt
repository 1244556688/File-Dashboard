package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScannedFile
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    files: List<ScannedFile>,
    isScanning: Boolean,
    progress: Int,
    total: Int,
    scanResult: String?,
    appLanguage: AppLanguage,
    onScanClick: () -> Unit,
    onDemoClick: () -> Unit,
    onClearClick: () -> Unit,
    onFileClick: (ScannedFile) -> Unit,
    modifier: Modifier = Modifier
) {
    // Computations
    val totalSize = files.sumOf { it.fileSize }
    val formattedTotalSize = formatFileSize(totalSize)
    val avgConfidence = if (files.isNotEmpty()) files.map { it.confidence }.average().toInt() else 0

    val duplicateGroups = files.groupBy { it.fileName + "_" + it.fileSize }
        .filter { it.value.size > 1 }
        .values.flatten()

    val baseTypeGroups = files.groupBy { it.baseType }
    val semanticClassGroups = files.groupBy { it.semanticClass }

    val recentFiles = files.sortedByDescending { it.addedDate }.take(4)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // AI Mainframe Status / Header Banner
        item {
            AIBanner(filesCount = files.size, avgConfidence = avgConfidence, formattedTotalSize = formattedTotalSize, appLanguage = appLanguage)
        }

        // Active Scan Progress Console
        if (isScanning || scanResult != null) {
            item {
                ScanConsoleCard(
                    isScanning = isScanning,
                    progress = progress,
                    total = total,
                    scanResult = scanResult,
                    appLanguage = appLanguage,
                    onClearLogs = onClearClick
                )
            }
        }

        // Actions Terminal Panel
        item {
            ActionsTerminal(
                onScanClick = onScanClick,
                onDemoClick = onDemoClick,
                onClearClick = onClearClick,
                hasFiles = files.isNotEmpty(),
                appLanguage = appLanguage
            )
        }

        // Storage Space Analysis Card (Glowing Progress Bar)
        item {
            StorageAnalysisCard(totalSize = totalSize, files = files, appLanguage = appLanguage)
        }

        // Distribution Charts Side by Side
        item {
            DistributionChartsRow(
                baseTypeGroups = baseTypeGroups,
                semanticClassGroups = semanticClassGroups,
                totalFilesCount = files.size,
                appLanguage = appLanguage
            )
        }

        // Duplicates / Redundancies Alert
        if (duplicateGroups.isNotEmpty()) {
            item {
                DuplicatesAlertCard(duplicateGroups = duplicateGroups, appLanguage = appLanguage, onFileClick = onFileClick)
            }
        }

        // Recent Scanned Real-Time Files Activity Feed
        item {
            RecentActivityFeed(recentFiles = recentFiles, appLanguage = appLanguage, onFileClick = onFileClick)
        }
    }
}

@Composable
fun AIBanner(filesCount: Int, avgConfidence: Int, formattedTotalSize: String, appLanguage: AppLanguage) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = Translation.get(appLanguage, "ai_core_mainframe"),
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = Translation.get(appLanguage, "system_active"),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Glowing Node Indicator
                Box(
                    modifier = Modifier
                        .size(12.0.dp)
                        .background(CyberGreen, RoundedCornerShape(6.dp))
                        .border(3.dp, CyberGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryStat(label = Translation.get(appLanguage, "scanned_sectors"), value = filesCount.toString(), color = CyberPurple)
                TelemetryStat(label = Translation.get(appLanguage, "total_volume"), value = formattedTotalSize, color = CyberCyan)
                TelemetryStat(label = Translation.get(appLanguage, "ai_accuracy"), value = "$avgConfidence%", color = CyberGreen)
            }
        }
    }
}

@Composable
fun TelemetryStat(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            color = IceBlue.copy(alpha = 0.6f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp
        )
        Text(
            text = value,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ScanConsoleCard(
    isScanning: Boolean,
    progress: Int,
    total: Int,
    scanResult: String?,
    appLanguage: AppLanguage,
    onClearLogs: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Translation.get(appLanguage, "terminal_scan_logs"),
                        color = CyberGreen,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isScanning) {
                    CircularProgressIndicator(
                        color = CyberGreen,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Text output
            Text(
                text = scanResult ?: Translation.get(appLanguage, "idle"),
                color = IceBlue,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            if (isScanning && total > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress.toFloat() / total.toFloat() },
                    color = CyberGreen,
                    trackColor = CyberGreen.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
                Text(
                    text = String.format(Translation.get(appLanguage, "sector_progress"), progress.toString(), total.toString()),
                    color = CyberGreen.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ActionsTerminal(
    onScanClick: () -> Unit,
    onDemoClick: () -> Unit,
    onClearClick: () -> Unit,
    hasFiles: Boolean,
    appLanguage: AppLanguage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onScanClick,
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .weight(1.2f)
                .height(44.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = Translation.get(appLanguage, "scan_directory"),
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        Button(
            onClick = onDemoClick,
            colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .border(1.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = CyberPurple,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = Translation.get(appLanguage, "demo_corpus"),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (hasFiles) {
            IconButton(
                onClick = onClearClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(CyberSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, CyberRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Reset Mainframe",
                    tint = CyberRed
                )
            }
        }
    }
}

@Composable
fun StorageAnalysisCard(totalSize: Long, files: List<ScannedFile>, appLanguage: AppLanguage) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = Translation.get(appLanguage, "storage_matrix"),
                color = CyberCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment progress bar representing document, image, audio, video etc.
            val fileTypesList = listOf("DOCUMENT", "IMAGE", "VIDEO", "APK", "AUDIO", "ARCHIVE", "UNKNOWN")
            val baseTypeColors = mapOf(
                "DOCUMENT" to CyberCyan,
                "IMAGE" to CyberPurple,
                "VIDEO" to CyberGreen,
                "APK" to NeonOrange,
                "AUDIO" to CyberPink,
                "ARCHIVE" to Color(0xFF007FFF),
                "UNKNOWN" to Color.Gray
            )

            val typeSizes = fileTypesList.associateWith { type ->
                files.filter { it.baseType == type }.sumOf { it.fileSize }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF1E293B))
            ) {
                if (totalSize > 0) {
                    typeSizes.forEach { (type, size) ->
                        val weight = size.toFloat() / totalSize
                        if (weight > 0.01f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(weight)
                                    .background(baseTypeColors[type] ?: Color.Gray)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray.copy(alpha = 0.3f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic grid list of categories and size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StorageCategoryRow(Translation.get(appLanguage, "docs"), formatFileSize(typeSizes["DOCUMENT"] ?: 0L), CyberCyan)
                    StorageCategoryRow(Translation.get(appLanguage, "visual"), formatFileSize(typeSizes["IMAGE"] ?: 0L), CyberPurple)
                    StorageCategoryRow(Translation.get(appLanguage, "media"), formatFileSize(typeSizes["VIDEO"] ?: 0L), CyberGreen)
                    StorageCategoryRow(Translation.get(appLanguage, "apks"), formatFileSize(typeSizes["APK"] ?: 0L), NeonOrange)
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    StorageCategoryRow(Translation.get(appLanguage, "audio"), formatFileSize(typeSizes["AUDIO"] ?: 0L), CyberPink)
                    StorageCategoryRow(Translation.get(appLanguage, "archives"), formatFileSize(typeSizes["ARCHIVE"] ?: 0L), Color(0xFF007FFF))
                    StorageCategoryRow(Translation.get(appLanguage, "unknown_category"), formatFileSize(typeSizes["UNKNOWN"] ?: 0L), Color.Gray)
                }
            }
        }
    }
}

@Composable
fun StorageCategoryRow(label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = IceBlue.copy(alpha = 0.8f), fontSize = 11.sp)
        }
        Text(text = value, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DistributionChartsRow(
    baseTypeGroups: Map<String, List<ScannedFile>>,
    semanticClassGroups: Map<String, List<ScannedFile>>,
    totalFilesCount: Int,
    appLanguage: AppLanguage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(200.dp)
                .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Translation.get(appLanguage, "sector_type_dist"),
                    color = CyberPurple,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (totalFilesCount > 0) {
                    val entries = baseTypeGroups.map { (key, list) ->
                        ChartEntry(key, list.size.toFloat(), getBaseTypeColor(key))
                    }
                    GlowingPieChart(entries = entries, modifier = Modifier.size(100.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = String.format(Translation.get(appLanguage, "primary"), entries.maxByOrNull { it.value }?.label ?: "None"),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    EmptyTelemetryText(appLanguage)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(200.dp)
                .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Translation.get(appLanguage, "semantic_knowledge"),
                    color = CyberGreen,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (totalFilesCount > 0) {
                    val entries = semanticClassGroups.map { (key, list) ->
                        ChartEntry(key, list.size.toFloat(), getSemanticClassColor(key))
                    }
                    GlowingPieChart(entries = entries, modifier = Modifier.size(100.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = String.format(Translation.get(appLanguage, "dominant"), entries.maxByOrNull { it.value }?.label ?: "None"),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    EmptyTelemetryText(appLanguage)
                }
            }
        }
    }
}

data class ChartEntry(val label: String, val value: Float, val color: Color)

@Composable
fun GlowingPieChart(entries: List<ChartEntry>, modifier: Modifier = Modifier) {
    val total = entries.sumOf { it.value.toDouble() }.toFloat()

    Canvas(modifier = modifier) {
        var startAngle = -90f
        entries.forEach { entry ->
            val sweepAngle = (entry.value / total) * 360f
            drawArc(
                color = entry.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 18f, cap = StrokeCap.Round),
                size = Size(size.width - 20f, size.height - 20f),
                topLeft = Offset(10f, 10f)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun EmptyTelemetryText(appLanguage: AppLanguage) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = Translation.get(appLanguage, "empty_telemetry"),
            color = IceBlue.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun DuplicatesAlertCard(duplicateGroups: List<ScannedFile>, appLanguage: AppLanguage, onFileClick: (ScannedFile) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = NeonOrange,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Translation.get(appLanguage, "redundancy_logs"),
                    color = NeonOrange,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format(Translation.get(appLanguage, "redundancy_desc"), duplicateGroups.size.toString()),
                color = IceBlue.copy(alpha = 0.8f),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Show first 2 duplicates
            val distinctDups = duplicateGroups.distinctBy { it.fileName }.take(2)
            distinctDups.forEach { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { onFileClick(file) }
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                             imageVector = Icons.Default.CopyAll,
                             contentDescription = null,
                             tint = NeonOrange,
                             modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = file.fileName,
                            color = Color.White,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = formatFileSize(file.fileSize),
                        color = NeonOrange,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun RecentActivityFeed(recentFiles: List<ScannedFile>, appLanguage: AppLanguage, onFileClick: (ScannedFile) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = Translation.get(appLanguage, "recent_sectors"),
                color = CyberCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (recentFiles.isEmpty()) {
                Text(
                    text = Translation.get(appLanguage, "no_recent_scans"),
                    color = IceBlue.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                recentFiles.forEach { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFileClick(file) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = getBaseTypeIcon(file.baseType),
                                contentDescription = null,
                                tint = getBaseTypeColor(file.baseType),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = file.fileName,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = file.aiPurpose,
                                    color = IceBlue.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Confidence meter tag
                        Box(
                            modifier = Modifier
                                .border(1.dp, getConfidenceColor(file.confidence).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .background(getConfidenceColor(file.confidence).copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI ${file.confidence}%",
                                color = getConfidenceColor(file.confidence),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Divider(color = CyberBorder.copy(alpha = 0.4f))
                }
            }
        }
    }
}

// Helpers
fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}

fun getBaseTypeColor(type: String): Color {
    return when (type.uppercase()) {
        "DOCUMENT" -> CyberCyan
        "IMAGE" -> CyberPurple
        "VIDEO" -> CyberGreen
        "APK" -> NeonOrange
        "AUDIO" -> CyberPink
        "ARCHIVE" -> Color(0xFF007FFF)
        else -> Color.Gray
    }
}

fun getSemanticClassColor(clazz: String): Color {
    return when (clazz.uppercase()) {
        "STUDY" -> CyberCyan
        "WORK" -> CyberPurple
        "GAMING" -> CyberGreen
        "SCREENSHOT" -> CyberPink
        "CODE" -> NeonOrange
        "JUNK" -> CyberRed
        else -> Color.LightGray
    }
}

fun getConfidenceColor(conf: Int): Color {
    return when {
        conf >= 85 -> CyberGreen
        conf >= 60 -> NeonOrange
        else -> CyberRed
    }
}

fun getBaseTypeIcon(type: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type.uppercase()) {
        "DOCUMENT" -> Icons.Default.Description
        "IMAGE" -> Icons.Default.Image
        "VIDEO" -> Icons.Default.Videocam
        "APK" -> Icons.Default.Android
        "AUDIO" -> Icons.Default.AudioFile
        "ARCHIVE" -> Icons.Default.FolderZip
        else -> Icons.Default.HelpOutline
    }
}

fun getSemanticIcon(clazz: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (clazz.uppercase()) {
        "STUDY" -> Icons.Default.School
        "WORK" -> Icons.Default.BusinessCenter
        "GAMING" -> Icons.Default.SportsEsports
        "SCREENSHOT" -> Icons.Default.Screenshot
        "CODE" -> Icons.Default.Code
        "JUNK" -> Icons.Default.AutoDelete
        else -> Icons.Default.Source
    }
}
