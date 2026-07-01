package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ScannedFile
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailsDialog(
    file: ScannedFile,
    appLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSubmitFeedback: (
        file: ScannedFile,
        newBaseType: String,
        newSemanticClass: String,
        newPurpose: String,
        learnAsRule: Boolean,
        ruleType: String
    ) -> Unit,
    onDeleteRecord: (ScannedFile) -> Unit
) {
    var isCalibrating by remember { mutableStateOf(false) }

    // Correction state variables
    val baseTypes = listOf("DOCUMENT", "IMAGE", "VIDEO", "APK", "AUDIO", "ARCHIVE", "UNKNOWN")
    val semanticClasses = listOf("STUDY", "WORK", "GAMING", "SCREENSHOT", "CODE", "JUNK", "OTHER")

    var selectedBaseType by remember { mutableStateOf(file.baseType) }
    var selectedSemanticClass by remember { mutableStateOf(file.semanticClass) }
    var correctedPurpose by remember { mutableStateOf(file.aiPurpose) }
    var learnAsRule by remember { mutableStateOf(false) }
    var ruleType by remember { mutableStateOf("NAME_CONTAINS") } // "NAME_CONTAINS" or "EXTENSION"

    var showBaseDropdown by remember { mutableStateOf(false) }
    var showSemanticDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Translation.get(appLanguage, "sector_telemetry"),
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // File name & full path
                Text(
                    text = file.fileName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = file.filePath,
                    color = IceBlue.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Standard Specifications Grid
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, CyberBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        MetadataRow(Translation.get(appLanguage, "file_size"), formatFileSize(file.fileSize))
                        MetadataRow(Translation.get(appLanguage, "mime_type"), file.mimeType ?: "application/octet-stream")
                        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        MetadataRow(Translation.get(appLanguage, "registry_date"), df.format(Date(file.addedDate)))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI Prediction Analysis Block
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Translation.get(appLanguage, "ai_semantic_inference"),
                                color = CyberPurple,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )

                            // Accuracy Tag
                            Box(
                                modifier = Modifier
                                    .background(getConfidenceColor(file.confidence).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .border(1.dp, getConfidenceColor(file.confidence).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = String.format(Translation.get(appLanguage, "confidence"), file.confidence.toString()),
                                    color = getConfidenceColor(file.confidence),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category rows
                        AIMetadataRow("BASE TYPE", file.baseType, getBaseTypeColor(file.baseType))
                        AIMetadataRow("SEMANTIC CLASS", file.semanticClass, getSemanticClassColor(file.semanticClass))
                        AIMetadataRow("INFERRED PURPOSE", file.aiPurpose, Color.White)

                        // If user has corrected it before, show "CALIBRATED" watermark
                        if (file.userCorrected) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = Translation.get(appLanguage, "calibrated_watermark"),
                                    color = CyberGreen,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // If unknown baseType, show probabilities guesses breakdown
                if (file.probabilities != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonOrange.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = Translation.get(appLanguage, "guess_breakdown"),
                                color = NeonOrange,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val probList = file.probabilities.split(",")
                            probList.forEach { prob ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val parts = prob.split(":")
                                    val label = parts.getOrNull(0)?.trim() ?: "Guess"
                                    val pct = parts.getOrNull(1)?.trim() ?: "0%"

                                    Text(text = label, color = IceBlue, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Text(text = pct, color = NeonOrange, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Calibration Area
                AnimatedVisibility(visible = isCalibrating) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = Translation.get(appLanguage, "calibrate_core"),
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Base Type dropdown replacement selector buttons
                        Text(text = Translation.get(appLanguage, "base_type_label"), color = IceBlue, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showBaseDropdown = !showBaseDropdown }
                                .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
                                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selectedBaseType, color = getBaseTypeColor(selectedBaseType), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Icon(imageVector = if (showBaseDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                        }

                        if (showBaseDropdown) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                                modifier = Modifier.fillMaxWidth().border(1.dp, CyberBorder)
                            ) {
                                Column {
                                    baseTypes.forEach { type ->
                                        Text(
                                            text = type,
                                            color = getBaseTypeColor(type),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedBaseType = type
                                                    showBaseDropdown = false
                                                }
                                                .padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Semantic Class selector
                        Text(text = Translation.get(appLanguage, "semantic_class_label"), color = IceBlue, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSemanticDropdown = !showSemanticDropdown }
                                .background(CyberSurfaceVariant, RoundedCornerShape(6.dp))
                                .border(1.dp, CyberBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selectedSemanticClass, color = getSemanticClassColor(selectedSemanticClass), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Icon(imageVector = if (showSemanticDropdown) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                        }

                        if (showSemanticDropdown) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                                modifier = Modifier.fillMaxWidth().border(1.dp, CyberBorder)
                            ) {
                                Column {
                                    semanticClasses.forEach { clazz ->
                                        Text(
                                            text = clazz,
                                            color = getSemanticClassColor(clazz),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedSemanticClass = clazz
                                                    showSemanticDropdown = false
                                                }
                                                .padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom purpose input
                        Text(text = Translation.get(appLanguage, "predicted_use"), color = IceBlue, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = correctedPurpose,
                            onValueChange = { correctedPurpose = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CyberSurfaceVariant,
                                unfocusedContainerColor = CyberSurfaceVariant,
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberBorder
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Learn Rule Option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = learnAsRule,
                                onCheckedChange = { learnAsRule = it },
                                colors = CheckboxDefaults.colors(checkedColor = CyberCyan, uncheckedColor = CyberBorder)
                            )
                            Text(text = Translation.get(appLanguage, "teach_ai_rule"), color = Color.White, fontSize = 12.sp)
                        }

                        if (learnAsRule) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { ruleType = "NAME_CONTAINS" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (ruleType == "NAME_CONTAINS") CyberPurple else CyberSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = Translation.get(appLanguage, "match_name"), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }

                                Button(
                                    onClick = { ruleType = "EXTENSION" },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (ruleType == "EXTENSION") CyberPurple else CyberSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = Translation.get(appLanguage, "match_ext"), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Save Correction Button
                        Button(
                            onClick = {
                                onSubmitFeedback(
                                    file,
                                    selectedBaseType,
                                    selectedSemanticClass,
                                    correctedPurpose,
                                    learnAsRule,
                                    ruleType
                                )
                                isCalibrating = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.SettingsSuggest, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = Translation.get(appLanguage, "apply_calibration"), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isCalibrating) {
                        Button(
                            onClick = { isCalibrating = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = Translation.get(appLanguage, "calibrate"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { isCalibrating = false },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text(text = Translation.get(appLanguage, "cancel"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            onDeleteRecord(file)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CyberRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = CyberRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Translation.get(appLanguage, "purge"), color = CyberRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = IceBlue.copy(alpha = 0.6f), fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AIMetadataRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = IceBlue.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = valueColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End
        )
    }
}
