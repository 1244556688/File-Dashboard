package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScannedFile
import com.example.ui.theme.*

@Composable
fun KnowledgeMapScreen(
    files: List<ScannedFile>,
    appLanguage: AppLanguage,
    onFileClick: (ScannedFile) -> Unit,
    modifier: Modifier = Modifier
) {
    // Map files to distinct functional sectors
    val studyFiles = files.filter { it.semanticClass == "STUDY" || it.semanticClass == "CODE" }
    val gamingFiles = files.filter { it.semanticClass == "GAMING" }
    val workFiles = files.filter { it.semanticClass == "WORK" }
    val junkFiles = files.filter { it.semanticClass == "JUNK" }
    val otherFiles = files.filter { it.semanticClass != "STUDY" && it.semanticClass != "CODE" &&
            it.semanticClass != "GAMING" && it.semanticClass != "WORK" && it.semanticClass != "JUNK" }

    var expandedSector by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Explanatory Subheader
        item {
            Column {
                Text(
                    text = Translation.get(appLanguage, "semantic_matrix_title"),
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = Translation.get(appLanguage, "semantic_matrix_desc"),
                    color = IceBlue.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        }

        // Sector 1: Study Material
        item {
            KnowledgeSectorCard(
                title = Translation.get(appLanguage, "sector_study"),
                subtitle = Translation.get(appLanguage, "sector_study_sub"),
                icon = Icons.Default.School,
                color = CyberCyan,
                sectorFiles = studyFiles,
                isExpanded = expandedSector == "STUDY",
                appLanguage = appLanguage,
                onToggleExpand = { expandedSector = if (expandedSector == "STUDY") null else "STUDY" },
                onFileClick = onFileClick
            )
        }

        // Sector 2: Gaming Records
        item {
            KnowledgeSectorCard(
                title = Translation.get(appLanguage, "sector_gaming"),
                subtitle = Translation.get(appLanguage, "sector_gaming_sub"),
                icon = Icons.Default.SportsEsports,
                color = CyberGreen,
                sectorFiles = gamingFiles,
                isExpanded = expandedSector == "GAMING",
                appLanguage = appLanguage,
                onToggleExpand = { expandedSector = if (expandedSector == "GAMING") null else "GAMING" },
                onFileClick = onFileClick
            )
        }

        // Sector 3: Work Files
        item {
            KnowledgeSectorCard(
                title = Translation.get(appLanguage, "sector_work"),
                subtitle = Translation.get(appLanguage, "sector_work_sub"),
                icon = Icons.Default.BusinessCenter,
                color = CyberPurple,
                sectorFiles = workFiles,
                isExpanded = expandedSector == "WORK",
                appLanguage = appLanguage,
                onToggleExpand = { expandedSector = if (expandedSector == "WORK") null else "WORK" },
                onFileClick = onFileClick
            )
        }

        // Sector 4: Junk & Temp
        item {
            KnowledgeSectorCard(
                title = Translation.get(appLanguage, "sector_junk"),
                subtitle = Translation.get(appLanguage, "sector_junk_sub"),
                icon = Icons.Default.AutoDelete,
                color = CyberRed,
                sectorFiles = junkFiles,
                isExpanded = expandedSector == "JUNK",
                appLanguage = appLanguage,
                onToggleExpand = { expandedSector = if (expandedSector == "JUNK") null else "JUNK" },
                onFileClick = onFileClick
            )
        }

        // Sector 5: Other Sectors
        item {
            KnowledgeSectorCard(
                title = Translation.get(appLanguage, "sector_other"),
                subtitle = Translation.get(appLanguage, "sector_other_sub"),
                icon = Icons.Default.Source,
                color = Color.LightGray,
                sectorFiles = otherFiles,
                isExpanded = expandedSector == "OTHER",
                appLanguage = appLanguage,
                onToggleExpand = { expandedSector = if (expandedSector == "OTHER") null else "OTHER" },
                onFileClick = onFileClick
            )
        }
    }
}

@Composable
fun KnowledgeSectorCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    sectorFiles: List<ScannedFile>,
    isExpanded: Boolean,
    appLanguage: AppLanguage,
    onToggleExpand: () -> Unit,
    onFileClick: (ScannedFile) -> Unit
) {
    val totalSize = sectorFiles.sumOf { it.fileSize }

    Card(
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        color.copy(alpha = if (isExpanded) 0.8f else 0.25f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header clickable row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            color = IceBlue.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "${sectorFiles.size} Files",
                        color = color,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = formatFileSize(totalSize),
                        color = IceBlue.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Expanding file list
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(250)),
                exit = shrinkVertically(animationSpec = tween(250))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = CyberBorder.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))

                    if (sectorFiles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Translation.get(appLanguage, "sector_offline"),
                                color = IceBlue.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        sectorFiles.forEach { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFileClick(file) }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getBaseTypeIcon(file.baseType),
                                        contentDescription = null,
                                        tint = getBaseTypeColor(file.baseType).copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = file.fileName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = file.aiPurpose,
                                            color = IceBlue.copy(alpha = 0.7f),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = formatFileSize(file.fileSize),
                                        color = IceBlue.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, getConfidenceColor(file.confidence).copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                                            .background(getConfidenceColor(file.confidence).copy(alpha = 0.05f), RoundedCornerShape(3.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "${file.confidence}%",
                                            color = getConfidenceColor(file.confidence),
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Divider(color = CyberBorder.copy(alpha = 0.15f))
                        }
                    }
                }
            }
        }
    }
}
