package com.example.echorooms.ui.create

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.echorooms.EchoRoomsApplication
import com.example.echorooms.data.database.entity.MoodTheme
import com.example.echorooms.data.database.entity.RoomEntity
import com.example.echorooms.theme.GlassBorder
import com.example.echorooms.theme.NeonCyan
import com.example.echorooms.theme.NeonPink
import com.example.echorooms.theme.TextGray
import com.example.echorooms.theme.TextSilver
import com.example.echorooms.ui.components.AmbientBackground
import com.example.echorooms.ui.components.glassmorphic
import com.example.echorooms.ui.components.neonGlow
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import com.example.echorooms.data.database.entity.CustomThemeData
import com.example.echorooms.data.database.entity.toCustomThemeData
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateRoomScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as EchoRoomsApplication
    val viewModel: CreateRoomViewModel = viewModel { CreateRoomViewModel(app.roomRepository) }

    val scrollState = rememberScrollState()
    var showCustomThemeDialog by remember { mutableStateOf(false) }

    // Derive CustomThemeData based on preset theme or custom JSON settings
    val currentThemeData = remember(viewModel.moodTheme, viewModel.customThemeJson) {
        val json = viewModel.customThemeJson
        if (!json.isNullOrBlank()) {
            try {
                val obj = JSONObject(json)
                CustomThemeData(
                    displayName = obj.optString("displayName", "Custom Theme"),
                    primaryColor = Color(android.graphics.Color.parseColor(obj.getString("primary"))),
                    accentColor = Color(android.graphics.Color.parseColor(obj.getString("accent"))),
                    glowColor = Color(android.graphics.Color.parseColor(obj.getString("glow"))),
                    particleType = obj.optString("particleType", viewModel.moodTheme.name),
                    soundscape = obj.optString("soundscape", viewModel.moodTheme.name)
                )
            } catch (e: Exception) {
                viewModel.moodTheme.toCustomThemeData()
            }
        } else {
            viewModel.moodTheme.toCustomThemeData()
        }
    }

    // Activity Result Launcher for Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        viewModel.coverImageUri = uri
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collectLatest { success ->
            if (success) {
                onBack()
            }
        }
    }

    AmbientBackground(themeData = currentThemeData) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEW CHAMBER",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(bottom = 32.dp)
            ) {
                // Section 1: Live Card Preview
                Text(
                    text = "LIVE PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 8.dp)
                ) {
                    StaticPreviewCard(
                        title = if (viewModel.title.isEmpty()) "Unnamed Chamber" else viewModel.title,
                        description = viewModel.description,
                        themeData = currentThemeData,
                        iconEmoji = viewModel.iconEmoji,
                        coverImageUriString = viewModel.coverImageUri?.toString(),
                        isTimeCapsule = viewModel.isTimeCapsule
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Form Container
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .glassmorphic(cornerRadius = 20.dp)
                        .padding(20.dp)
                ) {
                    // Field 1: Title Input
                    Text(
                        text = "CHAMBER NAME",
                        style = MaterialTheme.typography.labelSmall,
                        color = currentThemeData.accentColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = viewModel.title,
                            onValueChange = { viewModel.title = it },
                            textStyle = TextStyle(color = TextSilver, fontSize = 14.sp),
                            cursorBrush = SolidColor(currentThemeData.accentColor),
                            decorationBox = { innerTextField ->
                                if (viewModel.title.isEmpty()) {
                                    Text(
                                        text = "e.g., Summer Rain Echoes",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextGray
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field 2: Description Input
                    Text(
                        text = "ATMOSPHERIC DESCRIPTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = currentThemeData.accentColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = viewModel.description,
                            onValueChange = { viewModel.description = it },
                            textStyle = TextStyle(color = TextSilver, fontSize = 14.sp),
                            cursorBrush = SolidColor(currentThemeData.accentColor),
                            decorationBox = { innerTextField ->
                                if (viewModel.description.isEmpty()) {
                                    Text(
                                        text = "Describe the feeling of this space...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextGray
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 2: Mood Theme Picker
                Text(
                    text = "CHOOSE ATMOSPHERE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MoodTheme.values()) { theme ->
                        val isSelected = viewModel.moodTheme == theme && viewModel.customThemeJson.isNullOrBlank()
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(120.dp)
                                .glassmorphic(
                                    cornerRadius = 12.dp,
                                    borderColor = if (isSelected) theme.accent() else GlassBorder,
                                    backgroundColor = if (isSelected) theme
                                        .primary()
                                        .copy(alpha = 0.1f) else Color(0x0CFFFFFF)
                                )
                                .clickable {
                                    viewModel.customThemeJson = null
                                    viewModel.onThemeSelected(theme)
                                }
                                .padding(12.dp)
                        ) {
                            Text(text = theme.iconDefault, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = theme.displayName,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = theme.description,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGray,
                                fontSize = 9.sp,
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Custom Theme Creator Card
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .glassmorphic(cornerRadius = 16.dp)
                        .padding(16.dp)
                ) {
                    if (viewModel.customThemeJson.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CUSTOM ATMOSPHERE DESIGNER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = currentThemeData.accentColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Synthesize custom colors, particle drift types, and sound hums.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGray
                                )
                            }
                            Button(
                                onClick = { showCustomThemeDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = currentThemeData.accentColor.copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, currentThemeData.accentColor),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+ Synthesize",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SYNTHESIZED ACTIVE ATMOSPHERE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = currentThemeData.accentColor
                                )
                                Row {
                                    Text(
                                        text = "Reset",
                                        color = NeonPink,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier
                                            .clickable { viewModel.customThemeJson = null }
                                            .padding(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Edit",
                                        color = currentThemeData.accentColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier
                                            .clickable { showCustomThemeDialog = true }
                                            .padding(8.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(currentThemeData.primaryColor)
                                        .border(1.dp, currentThemeData.glowColor.copy(alpha = 0.5f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(currentThemeData.accentColor)
                                        .border(1.dp, currentThemeData.glowColor.copy(alpha = 0.5f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentThemeData.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Drift: ${currentThemeData.particleType}  |  Hum: ${currentThemeData.soundscape}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 3: Emoji Picker & Cover Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Emoji Selector
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .glassmorphic(cornerRadius = 16.dp)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "CHAMBER ICON",
                            style = MaterialTheme.typography.labelSmall,
                            color = currentThemeData.accentColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val emojis = listOf("⚡", "🌧", "🌅", "🚀", "💻", "🌙", "🎧", "📸", "🔮", "🔥", "🌊", "🔑")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            emojis.forEach { emoji ->
                                val isSelected = viewModel.iconEmoji == emoji
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) currentThemeData.accentColor.copy(alpha = 0.2f) else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) currentThemeData.accentColor else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { viewModel.iconEmoji = emoji }
                                ) {
                                    Text(text = emoji, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    // Cover Picker
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .glassmorphic(cornerRadius = 16.dp)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "COVER IMAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = currentThemeData.accentColor,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (viewModel.coverImageUri != null) {
                            AsyncImage(
                                model = viewModel.coverImageUri,
                                contentDescription = "Cover Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                            )
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                            ) {
                                Text(
                                    text = "+ Add Cover",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextGray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Section 4: Security & Archive Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .glassmorphic(cornerRadius = 16.dp)
                        .padding(16.dp)
                ) {
                    // Biometric Lock Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BIOMETRIC ENTRY LOCK",
                                style = MaterialTheme.typography.labelSmall,
                                color = currentThemeData.accentColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Require fingerprint authentication to enter this chamber.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        }
                        Switch(
                            checked = viewModel.isBiometricProtected,
                            onCheckedChange = { viewModel.isBiometricProtected = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = currentThemeData.accentColor,
                                checkedTrackColor = currentThemeData.primaryColor.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.HorizontalDivider(color = GlassBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Time Capsule Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TIME CAPSULE ARCHIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = currentThemeData.accentColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Seal this room until a future date is reached.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGray
                            )
                        }
                        Switch(
                            checked = viewModel.isTimeCapsule,
                            onCheckedChange = { viewModel.isTimeCapsule = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = currentThemeData.accentColor,
                                checkedTrackColor = currentThemeData.primaryColor.copy(alpha = 0.3f)
                            )
                        )
                    }

                    if (viewModel.isTimeCapsule) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Unlock Delay (Hours):",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSilver
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(36.dp)
                                    .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = viewModel.timeCapsuleUnlockHours,
                                    onValueChange = { viewModel.timeCapsuleUnlockHours = it },
                                    textStyle = TextStyle(
                                        color = TextSilver,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    cursorBrush = SolidColor(currentThemeData.accentColor),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section 5: Save/Submit Button
                Button(
                    onClick = { viewModel.saveRoom(context) },
                    enabled = viewModel.title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(50.dp)
                        .neonGlow(
                            color = if (viewModel.title.isNotBlank()) currentThemeData.glowColor else Color.Gray,
                            cornerRadius = 25.dp
                        )
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "A R C H I V E   C H A M B E R",
                            color = if (viewModel.title.isNotBlank()) Color.White else TextGray,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showCustomThemeDialog) {
        CustomThemeCreatorDialog(
            initialThemeJson = viewModel.customThemeJson,
            defaultPresetTheme = viewModel.moodTheme,
            onDismiss = { showCustomThemeDialog = false },
            onSave = { json ->
                viewModel.customThemeJson = json
                showCustomThemeDialog = false
            }
        )
    }
}

@Composable
fun CustomThemeCreatorDialog(
    initialThemeJson: String?,
    defaultPresetTheme: MoodTheme,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val defaultPrimaryHex = "#FF0080"
    val defaultAccentHex = "#00FFFF"
    val defaultGlowHex = "#FF0080"

    var name by remember { mutableStateOf("My Synesthesia") }
    var primaryColorHex by remember { mutableStateOf(defaultPrimaryHex) }
    var accentColorHex by remember { mutableStateOf(defaultAccentHex) }
    var glowColorHex by remember { mutableStateOf(defaultGlowHex) }
    var particleType by remember { mutableStateOf(defaultPresetTheme.name) }
    var soundscape by remember { mutableStateOf(defaultPresetTheme.name) }

    LaunchedEffect(initialThemeJson) {
        if (!initialThemeJson.isNullOrBlank()) {
            try {
                val obj = JSONObject(initialThemeJson)
                name = obj.optString("displayName", "My Synesthesia")
                primaryColorHex = obj.optString("primary", defaultPrimaryHex)
                accentColorHex = obj.optString("accent", defaultAccentHex)
                glowColorHex = obj.optString("glow", defaultGlowHex)
                particleType = obj.optString("particleType", defaultPresetTheme.name)
                soundscape = obj.optString("soundscape", defaultPresetTheme.name)
            } catch (e: Exception) {}
        }
    }

    val neonPresets = listOf(
        "#FF0080", // Pink
        "#00FFFF", // Cyan
        "#7B2FBE", // Purple
        "#00FF41", // Terminal Green
        "#FF6B35", // Orange
        "#FFD54F", // Gold
        "#7BA7D7", // Silver Blue
        "#FF1744"  // Crimson
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(cornerRadius = 24.dp)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "DIMENSIONAL SYNTHESIS",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                // Theme Name Input
                Column {
                    Text(
                        text = "THEME NAME",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Cyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = name,
                            onValueChange = { name = it },
                            textStyle = TextStyle(color = TextSilver, fontSize = 14.sp),
                            cursorBrush = SolidColor(Color.Cyan),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Primary Color Picker
                Column {
                    Text(
                        text = "PRIMARY GLOW CORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Cyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        neonPresets.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = primaryColorHex.lowercase() == hex.lowercase()
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        2.dp,
                                        if (isSelected) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { primaryColorHex = hex }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Hex: ", style = MaterialTheme.typography.bodySmall, color = TextGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        BasicTextField(
                            value = primaryColorHex,
                            onValueChange = { primaryColorHex = it },
                            textStyle = TextStyle(color = TextSilver, fontSize = 12.sp),
                            cursorBrush = SolidColor(Color.Cyan),
                            modifier = Modifier
                                .width(80.dp)
                                .border(1.dp, GlassBorder, RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        )
                    }
                }

                // Accent Color Picker
                Column {
                    Text(
                        text = "SECONDARY DRIFT COLOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Cyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        neonPresets.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = accentColorHex.lowercase() == hex.lowercase()
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        2.dp,
                                        if (isSelected) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { accentColorHex = hex }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Hex: ", style = MaterialTheme.typography.bodySmall, color = TextGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        BasicTextField(
                            value = accentColorHex,
                            onValueChange = { accentColorHex = it },
                            textStyle = TextStyle(color = TextSilver, fontSize = 12.sp),
                            cursorBrush = SolidColor(Color.Cyan),
                            modifier = Modifier
                                .width(80.dp)
                                .border(1.dp, GlassBorder, RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        )
                    }
                }

                // Glow Color Picker
                Column {
                    Text(
                        text = "ATMOSPHERIC GLOW TINT",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Cyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        neonPresets.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = glowColorHex.lowercase() == hex.lowercase()
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        2.dp,
                                        if (isSelected) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { glowColorHex = hex }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Hex: ", style = MaterialTheme.typography.bodySmall, color = TextGray)
                        Spacer(modifier = Modifier.width(4.dp))
                        BasicTextField(
                            value = glowColorHex,
                            onValueChange = { glowColorHex = it },
                            textStyle = TextStyle(color = TextSilver, fontSize = 12.sp),
                            cursorBrush = SolidColor(Color.Cyan),
                            modifier = Modifier
                                .width(80.dp)
                                .border(1.dp, GlassBorder, RoundedCornerShape(4.dp))
                                .padding(4.dp)
                        )
                    }
                }

                // Particle Preset Selector
                Column {
                    Text(
                        text = "PARTICLE EMISSION ENGINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Cyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MoodTheme.values().forEach { theme ->
                            val isSelected = particleType == theme.name
                            Box(
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (isSelected) Color.Cyan else GlassBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        if (isSelected) Color.Cyan.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { particleType = theme.name }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = theme.displayName,
                                    color = if (isSelected) Color.White else TextGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Soundscape Preset Selector
                Column {
                    Text(
                        text = "AUDITORY HUM SYNTH",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Cyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MoodTheme.values().forEach { theme ->
                            val isSelected = soundscape == theme.name
                            Box(
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (isSelected) Color.Cyan else GlassBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        if (isSelected) Color.Cyan.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { soundscape = theme.name }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = theme.displayName,
                                    color = if (isSelected) Color.White else TextGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "CANCEL",
                        color = NeonPink,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "SAVE SYNTHESIS",
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .clickable {
                                val finalPrimary = if (primaryColorHex.startsWith("#")) primaryColorHex else "#$primaryColorHex"
                                val finalAccent = if (accentColorHex.startsWith("#")) accentColorHex else "#$accentColorHex"
                                val finalGlow = if (glowColorHex.startsWith("#")) glowColorHex else "#$glowColorHex"

                                val json = JSONObject().apply {
                                    put("displayName", name.trim().ifEmpty { "My Custom Theme" })
                                    put("primary", finalPrimary)
                                    put("accent", finalAccent)
                                    put("glow", finalGlow)
                                    put("particleType", particleType)
                                    put("soundscape", soundscape)
                                }.toString()
                                onSave(json)
                            }
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StaticPreviewCard(
    title: String,
    description: String,
    themeData: CustomThemeData,
    iconEmoji: String,
    coverImageUriString: String?,
    isTimeCapsule: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .neonGlow(color = themeData.glowColor, cornerRadius = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0x0CFFFFFF), CircleShape)
                ) {
                    Text(text = iconEmoji, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (coverImageUriString != null) {
                AsyncImage(
                    model = coverImageUriString,
                    contentDescription = "Cover Image Preview",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(themeData.glowColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = themeData.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = themeData.accentColor
                )
            }

            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Preview Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray
                )

                if (isTimeCapsule) {
                    Text(
                        text = "🔒 LOCKED CAPSULE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonPink,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

