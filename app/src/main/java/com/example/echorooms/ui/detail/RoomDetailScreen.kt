package com.example.echorooms.ui.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.echorooms.EchoRoomsApplication
import com.example.echorooms.data.audio.AudioPlayerManager
import com.example.echorooms.data.audio.PlaybackState
import com.example.echorooms.data.audio.RecordingState
import com.example.echorooms.data.database.entity.MemoryEntry
import com.example.echorooms.data.database.entity.MemoryType
import com.example.echorooms.data.database.entity.MoodTheme
import com.example.echorooms.data.database.entity.RoomEntity
import com.example.echorooms.data.database.entity.CustomThemeData
import com.example.echorooms.data.database.entity.getThemeColorsAndAtmosphere
import com.example.echorooms.theme.GlassBorder
import com.example.echorooms.theme.NeonPink
import com.example.echorooms.theme.TextGray
import com.example.echorooms.theme.TextSilver
import com.example.echorooms.ui.components.AmbientBackground
import com.example.echorooms.ui.components.DefaultThemeData
import com.example.echorooms.ui.components.WaveformVisualizer
import com.example.echorooms.ui.components.DrawingCanvas
import com.example.echorooms.ui.components.EmotionSliders
import com.example.echorooms.ui.components.SynesthesiaAuraCanvas
import com.example.echorooms.ui.components.MilestoneConstellationMap
import com.example.echorooms.ui.components.glassmorphic
import com.example.echorooms.ui.components.neonGlow
import com.example.echorooms.security.BiometricAuthenticator
import androidx.fragment.app.FragmentActivity
import com.example.echorooms.data.export.AtmosphericExporter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import com.example.echorooms.ui.components.MarkdownText
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RoomDetailScreen(
    roomId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as EchoRoomsApplication
    val viewModel: RoomDetailViewModel = viewModel {
        RoomDetailViewModel(roomId, app.roomRepository, app.memoryRepository)
    }

    val room by viewModel.roomState.collectAsState()
    val entries by viewModel.filteredEntriesState.collectAsState()
    val allEntriesForExport by viewModel.entriesState.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTypes by viewModel.selectedTypes.collectAsState()
    val selectedWeathers by viewModel.selectedWeathers.collectAsState()

    // Parallax sensor binding
    val tiltOffset by com.example.echorooms.hardware.ParallaxSensorListener.tiltOffset.collectAsState()

    val themeData = remember(room) { room?.getThemeColorsAndAtmosphere() ?: DefaultThemeData }

    // Soundscape binding
    androidx.compose.runtime.DisposableEffect(themeData) {
        com.example.echorooms.data.audio.ProceduralSoundscapePlayer.start(themeData.soundscape)
        onDispose {
            com.example.echorooms.data.audio.ProceduralSoundscapePlayer.stop()
        }
    }

    // Biometric security state
    var isBiometricAuthenticated by remember { mutableStateOf(false) }
    var biometricError by remember { mutableStateOf<String?>(null) }

    // Form Overlay Control State
    var activeInputMode by remember { mutableStateOf<MemoryType?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var showSensoryVisuals by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.selectedImageUri = uri
            activeInputMode = MemoryType.IMAGE
        }
    }

    AmbientBackground(themeData = themeData) {
        val currentRoom = room
        if (currentRoom == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = NeonPink
                )
            }
        } else if (currentRoom.isBiometricProtected && !isBiometricAuthenticated) {
            BiometricLockOverlay(
                room = currentRoom,
                themeData = themeData,
                onAuthenticate = {
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        BiometricAuthenticator.authenticate(
                            activity = activity,
                            title = "Decrypt Chamber",
                            subtitle = "Verify identity to access ${currentRoom.title}",
                            onSuccess = {
                                isBiometricAuthenticated = true
                                biometricError = null
                            },
                            onError = { err ->
                                biometricError = err
                            }
                        )
                    } else {
                        isBiometricAuthenticated = true
                    }
                },
                biometricError = biometricError,
                onBack = onBack
            )
        } else if (currentRoom.isLocked()) {
            // Time Capsule Lock Overlay
            TimeCapsuleLockedPanel(room = currentRoom, themeData = themeData, viewModel = viewModel, onBack = onBack)
        } else {
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
                            text = "${currentRoom.iconEmoji} ${currentRoom.title.uppercase()}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                AtmosphericExporter.exportAndShareRoom(context, currentRoom, allEntriesForExport)
                            }
                        ) {
                            Text(text = "📤", fontSize = 18.sp)
                        }
                    }
                },
                modifier = modifier.fillMaxSize()
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Main Timeline Workspace
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Parallax Room Banner with holographic tilt depth
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .graphicsLayer {
                                    translationX = tiltOffset.first * 10f
                                    translationY = tiltOffset.second * 10f
                                }
                                .glassmorphic(cornerRadius = 16.dp)
                        ) {
                            if (currentRoom.coverImagePath != null) {
                                val file = File(currentRoom.coverImagePath)
                                if (file.exists()) {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().alpha(0.45f)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    themeData.primaryColor.copy(alpha = 0.25f),
                                                    themeData.glowColor.copy(alpha = 0.05f)
                                                )
                                            )
                                        )
                                )
                            }
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = currentRoom.title.uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp
                                )
                                if (currentRoom.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentRoom.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSilver,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // folding drawer sensory core
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .glassmorphic(cornerRadius = 12.dp)
                                .clickable { showSensoryVisuals = !showSensoryVisuals }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🌌", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SENSORY CORE & CONSTELLATIONS",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(text = if (showSensoryVisuals) "▼" else "▲", color = TextSilver, fontSize = 12.sp)
                        }

                        AnimatedVisibility(visible = showSensoryVisuals) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .glassmorphic(cornerRadius = 16.dp)
                                    .padding(12.dp)
                            ) {
                                val emotions by viewModel.emotionRatiosState.collectAsState()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                ) {
                                    SynesthesiaAuraCanvas(
                                        emotion = emotions,
                                        themeData = themeData,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "SYNESTHESIA AURA CORE",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = themeData.accentColor,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "Peace: ${(emotions.peace*100).toInt()}% | Warmth: ${(emotions.warmth*100).toInt()}% | Melancholy: ${(emotions.melancholy*100).toInt()}% | Anxiety: ${(emotions.anxiety*100).toInt()}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextGray,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val milestoneEntries = remember(entries) {
                                    entries.filter { it.type == MemoryType.MILESTONE.name }
                                }
                                MilestoneConstellationMap(
                                    milestones = milestoneEntries,
                                    themeData = themeData,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Search & filter card bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .glassmorphic(cornerRadius = 16.dp)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.searchQuery.value = it },
                                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                                    cursorBrush = SolidColor(themeData.accentColor),
                                    decorationBox = { innerTextField ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(text = "🔍", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(modifier = Modifier.weight(1f)) {
                                                if (searchQuery.isEmpty()) {
                                                    Text("Search memories...", style = MaterialTheme.typography.bodyMedium, color = TextGray)
                                                }
                                                innerTextField()
                                            }
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(
                                                    onClick = { viewModel.searchQuery.value = "" },
                                                    modifier = Modifier.size(20.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Clear",
                                                        tint = TextGray,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (showFilters) themeData.accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, if (showFilters) themeData.accentColor else GlassBorder, RoundedCornerShape(8.dp))
                                        .clickable { showFilters = !showFilters }
                                ) {
                                    Text(text = "🎛️", fontSize = 16.sp)
                                }
                            }

                            AnimatedVisibility(visible = showFilters) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Text(
                                        text = "FILTER BY TYPE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = themeData.accentColor,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val typeOptions = listOf(
                                            MemoryType.TEXT.name to "📝 Text",
                                            MemoryType.VOICE.name to "🎙️ Voice",
                                            MemoryType.IMAGE.name to "📸 Image",
                                            MemoryType.SKETCH.name to "🎨 Sketch",
                                            MemoryType.MILESTONE.name to "⭐ Milestone"
                                        )
                                        typeOptions.forEach { (typeKey, label) ->
                                            val isSelected = selectedTypes.contains(typeKey)
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (isSelected) themeData.accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) themeData.accentColor else Color.White.copy(alpha = 0.1f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        val newSet = selectedTypes.toMutableSet()
                                                        if (isSelected) newSet.remove(typeKey) else newSet.add(typeKey)
                                                        viewModel.selectedTypes.value = newSet
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) Color.White else TextSilver,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Text(
                                        text = "FILTER BY WEATHER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = themeData.accentColor,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val weatherOptions = listOf("Clear", "Rainy", "Nebula", "Cyber Glow", "Midnight Fog", "Sunny Sunset")
                                        weatherOptions.forEach { weatherOpt ->
                                            val isSelected = selectedWeathers.contains(weatherOpt)
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (isSelected) themeData.accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) themeData.accentColor else Color.White.copy(alpha = 0.1f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable {
                                                        val newSet = selectedWeathers.toMutableSet()
                                                        if (isSelected) newSet.remove(weatherOpt) else newSet.add(weatherOpt)
                                                        viewModel.selectedWeathers.value = newSet
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = weatherOpt,
                                                    color = if (isSelected) Color.White else TextSilver,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Chronological Memory Timeline List
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            if (entries.isEmpty()) {
                                TimelineEmptyState()
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(bottom = 90.dp, top = 16.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(entries, key = { it.id }) { entry ->
                                        TimelineItemRow(
                                            entry = entry,
                                            themeData = themeData,
                                            audioPlayer = viewModel.audioPlayer,
                                            onPlayVoice = { viewModel.playVoiceEntry(entry.filePath!!) },
                                            onPauseVoice = { viewModel.pauseVoiceEntry() },
                                            onDelete = { viewModel.deleteEntry(entry) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Floating Panel for creating entries (Bottom bar overlay)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        AnimatedVisibility(
                            visible = activeInputMode == null,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            AddMemoryButtonsPanel(
                                onAddText = {
                                    viewModel.isMilestone = false
                                    activeInputMode = MemoryType.TEXT
                                },
                                onAddVoice = {
                                    viewModel.textTitle = ""
                                    viewModel.startVoiceRecording(context)
                                    activeInputMode = MemoryType.VOICE
                                },
                                onAddImage = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                onAddMilestone = {
                                    viewModel.isMilestone = true
                                    activeInputMode = MemoryType.TEXT
                                },
                                onAddSketch = {
                                    activeInputMode = MemoryType.SKETCH
                                }
                            )
                        }
                    }

                    // Overlay forms for typing notes
                    AnimatedVisibility(
                        visible = activeInputMode != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(
                            contentAlignment = Alignment.BottomCenter,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xCC000000))
                                .clickable {
                                    if (activeInputMode == MemoryType.VOICE) {
                                        viewModel.cancelVoiceRecording()
                                    }
                                    activeInputMode = null
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .glassmorphic(cornerRadius = 24.dp)
                                    .clickable(enabled = false) {} // Prevent click-through
                                    .padding(24.dp)
                            ) {
                                when (activeInputMode) {
                                    MemoryType.TEXT -> {
                                        TextNoteInputForm(
                                            viewModel = viewModel,
                                            themeData = themeData,
                                            onSave = {
                                                viewModel.saveTextNote()
                                                activeInputMode = null
                                            },
                                            onCancel = { activeInputMode = null }
                                        )
                                    }
                                    MemoryType.VOICE -> {
                                        VoiceRecordingForm(
                                            themeData = themeData,
                                            viewModel = viewModel,
                                            onSave = {
                                                viewModel.stopVoiceRecording(viewModel.textTitle)
                                                activeInputMode = null
                                            },
                                            onCancel = {
                                                viewModel.cancelVoiceRecording()
                                                activeInputMode = null
                                            }
                                        )
                                    }
                                    MemoryType.IMAGE -> {
                                        ImageCaptureForm(
                                            viewModel = viewModel,
                                            themeData = themeData,
                                            onSave = {
                                                viewModel.saveImageMemory(context)
                                                activeInputMode = null
                                            },
                                            onCancel = { activeInputMode = null }
                                        )
                                    }
                                    MemoryType.SKETCH -> {
                                        DrawingCanvas(
                                            onSave = { sketchPath ->
                                                viewModel.saveSketchMemory(sketchPath)
                                                activeInputMode = null
                                            },
                                            onCancel = { activeInputMode = null },
                                            context = context
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeCapsuleLockedPanel(
    room: RoomEntity,
    themeData: CustomThemeData,
    viewModel: RoomDetailViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val hapticManager = remember { com.example.echorooms.hardware.HapticManager(context) }
    
    // Ticker countdown state
    var remainingTimeMs by remember { mutableStateOf(0L) }
    LaunchedEffect(room.timeCapsuleUnlockDate) {
        while (true) {
            val unlockTime = room.timeCapsuleUnlockDate ?: 0L
            val now = System.currentTimeMillis()
            remainingTimeMs = (unlockTime - now).coerceAtLeast(0L)
            if (remainingTimeMs == 0L) {
                viewModel.unlockRoom()
                break
            }
            kotlinx.coroutines.delay(500)
        }
    }

    val countdownText = remember(remainingTimeMs) {
        val totalSecs = remainingTimeMs / 1000
        val secs = totalSecs % 60
        val mins = (totalSecs / 60) % 60
        val hours = (totalSecs / 3600) % 24
        val days = totalSecs / 86400
        if (days > 0) {
            "${days}d %02d:%02d:%02d".format(hours, mins, secs)
        } else {
            "%02d:%02d:%02d".format(hours, mins, secs)
        }
    }

    // Time lock elapsed progress
    val totalLockDuration = remember(room.createdAt, room.timeCapsuleUnlockDate) {
        val unlock = room.timeCapsuleUnlockDate ?: System.currentTimeMillis()
        (unlock - room.createdAt).coerceAtLeast(1000L)
    }
    
    val timeProgress = remember(remainingTimeMs, totalLockDuration) {
        val elapsed = totalLockDuration - remainingTimeMs
        (elapsed.toFloat() / totalLockDuration.toFloat()).coerceIn(0f, 1f)
    }

    // Manual ritual progress states
    var isPressing by remember { mutableStateOf(false) }
    var ritualProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isPressing) {
        if (isPressing) {
            val startTime = System.currentTimeMillis()
            val duration = 3000f // 3 seconds
            while (ritualProgress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                ritualProgress = (elapsed / duration).coerceIn(0f, 1f)
                
                // Tick haptic pulse
                hapticManager.vibrate(longArrayOf(0, (10 + ritualProgress * 30).toLong()))
                kotlinx.coroutines.delay(80)
            }
            // Complete decryption
            hapticManager.playExplosion()
            viewModel.unlockRoom()
        } else {
            // Fade out progress on release
            if (ritualProgress < 1f) {
                while (ritualProgress > 0f) {
                    ritualProgress = (ritualProgress - 0.1f).coerceAtLeast(0f)
                    kotlinx.coroutines.delay(30)
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        // Neon Progress Ring with Lock Icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(180.dp)
        ) {
            // 1. Particle Glow backing
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .blur(24.dp)
                    .background(themeData.glowColor.copy(alpha = 0.25f), CircleShape)
            )

            // 2. Neon Progress Canvas drawing circles
            val themeColor = themeData.accentColor
            val glowColor = themeData.glowColor
            Canvas(modifier = Modifier.size(150.dp)) {
                // Background Track
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                )
                // Time elapsed arc
                drawArc(
                    color = themeColor.copy(alpha = 0.35f),
                    startAngle = -90f,
                    sweepAngle = 360f * timeProgress,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                )
                // Manual bypass ritual charging arc
                if (ritualProgress > 0f) {
                    drawArc(
                        color = glowColor,
                        startAngle = -90f,
                        sweepAngle = 360f * ritualProgress,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 6.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
            }

            // Lock Emoji (scales up as ritual progress charges)
            val scale = 1f + ritualProgress * 0.2f
            Text(
                text = if (ritualProgress >= 1f) "🔓" else "🔒",
                fontSize = 52.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "E N C R Y P T E D   C A P S U L E",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "DECRYPTION CYCLE TIMER",
            style = MaterialTheme.typography.labelSmall,
            color = themeData.accentColor,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        
        Text(
            text = countdownText,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ritual interaction trigger area
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            themeData.primaryColor.copy(alpha = 0.1f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .border(1.dp, GlassBorder, RoundedCornerShape(30.dp))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val down = awaitFirstDown()
                            isPressing = true
                            waitForUpOrCancellation()
                            isPressing = false
                        }
                    }
                }
        ) {
            // Neon charging bar background overlay
            if (ritualProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    themeData.glowColor.copy(alpha = 0.4f * ritualProgress),
                                    themeData.accentColor.copy(alpha = 0.2f * ritualProgress)
                                )
                            )
                        )
                )
            }
            Text(
                text = if (isPressing) "MAINTAIN PRESSURE..." else "HOLD TO OVERRIDE DECRYPTION",
                color = if (isPressing) Color.White else TextSilver,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier
                .height(44.dp)
                .neonGlow(color = themeData.accentColor, cornerRadius = 22.dp)
        ) {
            Text(
                text = "RETURN TO ARCHIVE",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun TimelineEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(text = "📝", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "TIMELINE DORMANT",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This capsule is empty. Tap any icon below to register a voice echo, image, or note.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

@Composable
private fun TimelineItemRow(
    entry: MemoryEntry,
    themeData: CustomThemeData,
    audioPlayer: AudioPlayerManager,
    onPlayVoice: () -> Unit,
    onPauseVoice: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedTime = remember(entry.createdAt) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(entry.createdAt))
    }
    val formattedDate = remember(entry.createdAt) {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(entry.createdAt))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Left Column: Timeline Indicator Dot & Thread
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(themeData.primaryColor.copy(alpha = 0.15f))
                    .border(1.dp, themeData.accentColor, CircleShape)
            ) {
                Text(text = entry.typeIcon(), fontSize = 11.sp)
            }
            // Vertical timeline connecting thread
            Canvas(
                modifier = Modifier
                    .width(2.dp)
                    .height(130.dp)
            ) {
                drawLine(
                    color = themeData.accentColor.copy(alpha = 0.25f),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = 2f
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Column: Card Contents
        Column(modifier = Modifier.weight(1f)) {
            // Timestamp Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$formattedDate  •  $formattedTime",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray
                )

                if (entry.isDeletable) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Memory",
                            tint = TextGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Text(
                        text = "🔒",
                        fontSize = 11.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Body Card based on memory type
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (entry.getMemoryType() == MemoryType.MILESTONE) {
                            Modifier.neonGlow(color = themeData.glowColor, cornerRadius = 16.dp)
                        } else {
                            Modifier.glassmorphic(cornerRadius = 16.dp)
                        }
                    )
                    .padding(14.dp)
            ) {
                Column {
                    if (entry.title.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (entry.getMemoryType() == MemoryType.MILESTONE) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Milestone",
                                    tint = themeData.accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = entry.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (entry.getMemoryType() == MemoryType.MILESTONE) themeData.accentColor else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    when (entry.getMemoryType()) {
                        MemoryType.TEXT, MemoryType.MILESTONE -> {
                            MarkdownText(
                                text = entry.content,
                                color = TextSilver
                            )
                        }
                        MemoryType.IMAGE -> {
                            entry.filePath?.let { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = "Captured Memory",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }
                        MemoryType.SKETCH -> {
                            entry.filePath?.let { path ->
                                val file = File(path)
                                if (file.exists()) {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = "Handdrawn Sketch",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                            .background(Color(0xFF06070B))
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }
                        MemoryType.VOICE -> {
                            VoicePlayerBar(
                                entry = entry,
                                audioPlayer = audioPlayer,
                                themeData = themeData,
                                onPlay = onPlayVoice,
                                onPause = onPauseVoice
                            )
                        }
                    }

                    // Metadata tags row (weather and location)
                    if (entry.weather != null || entry.locationName != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            entry.weather?.let { w ->
                                Text(
                                    text = "🌤️ $w",
                                    fontSize = 9.sp,
                                    color = TextSilver,
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                            entry.locationName?.let { loc ->
                                Text(
                                    text = "📍 $loc",
                                    fontSize = 9.sp,
                                    color = TextSilver,
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Emotional signature blend bar
                    val totalEmotions = entry.valPeace + entry.valWarmth + entry.valAnxiety + entry.valMelancholy
                    if (totalEmotions > 0.05f) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                        ) {
                            if (entry.valPeace > 0f) {
                                Box(modifier = Modifier.weight(entry.valPeace).fillMaxHeight().background(Color(0xFFD4FF00)))
                            }
                            if (entry.valWarmth > 0f) {
                                Box(modifier = Modifier.weight(entry.valWarmth).fillMaxHeight().background(Color(0xFFFF4081)))
                            }
                            if (entry.valMelancholy > 0f) {
                                Box(modifier = Modifier.weight(entry.valMelancholy).fillMaxHeight().background(Color(0xFF00E5FF)))
                            }
                            if (entry.valAnxiety > 0f) {
                                Box(modifier = Modifier.weight(entry.valAnxiety).fillMaxHeight().background(Color(0xFF9C27B0)))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VoicePlayerBar(
    entry: MemoryEntry,
    audioPlayer: AudioPlayerManager,
    themeData: CustomThemeData,
    onPlay: () -> Unit,
    onPause: () -> Unit
) {
    val playState by audioPlayer.state.collectAsState()
    val playingPath by audioPlayer.currentFilePath.collectAsState()
    val currentPosition by audioPlayer.currentPosition.collectAsState()
    val duration by audioPlayer.duration.collectAsState()

    val isCurrentPlaying = playingPath == entry.filePath
    val isPlaying = isCurrentPlaying && playState == PlaybackState.PLAYING

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = {
                if (isPlaying) {
                    onPause()
                } else {
                    onPlay()
                }
            },
            modifier = Modifier
                .size(36.dp)
                .background(themeData.primaryColor.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, themeData.accentColor, CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) {
                    // Custom pause vector drawn as basic drawing or import
                    Icons.Default.Close
                } else {
                    Icons.Default.PlayArrow
                },
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = themeData.accentColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Progress Slider
        val progress = if (isCurrentPlaying && duration > 0) {
            currentPosition.toFloat() / duration.toFloat()
        } else {
            0f
        }

        Column(modifier = Modifier.weight(1f)) {
            Slider(
                value = progress,
                onValueChange = { percent ->
                    if (isCurrentPlaying && duration > 0) {
                        audioPlayer.seekTo((percent * duration).toInt())
                    }
                },
                colors = SliderDefaults.colors(
                    activeTrackColor = themeData.accentColor,
                    inactiveTrackColor = Color(0x1AFFFFFF),
                    thumbColor = themeData.accentColor
                ),
                modifier = Modifier.height(20.dp)
            )

            // Duration labels
            val totalSeconds = (entry.audioDurationMs ?: 0L) / 1000L
            val elapsedSeconds = if (isCurrentPlaying) currentPosition / 1000 else 0

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "%02d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray,
                    fontSize = 9.sp
                )
                Text(
                    text = "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun AddMemoryButtonsPanel(
    onAddText: () -> Unit,
    onAddVoice: () -> Unit,
    onAddImage: () -> Unit,
    onAddMilestone: () -> Unit,
    onAddSketch: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .glassmorphic(cornerRadius = 28.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        FloatingButton(emoji = "📝", onClick = onAddText)
        FloatingButton(emoji = "🎙️", onClick = onAddVoice)
        FloatingButton(emoji = "📸", onClick = onAddImage)
        FloatingButton(emoji = "🎨", onClick = onAddSketch)
        FloatingButton(emoji = "⭐", onClick = onAddMilestone)
    }
}

@Composable
private fun FloatingButton(
    emoji: String,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color(0x0CFFFFFF))
            .border(1.dp, GlassBorder, CircleShape)
            .clickable { onClick() }
    ) {
        Text(text = emoji, fontSize = 16.sp)
    }
}

@Composable
private fun TextNoteInputForm(
    viewModel: RoomDetailViewModel,
    themeData: CustomThemeData,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (viewModel.isMilestone) "RECORD MILESTONE EVENT" else "ADD TIMELINE NOTE",
            style = MaterialTheme.typography.labelSmall,
            color = themeData.accentColor,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Title Input
        BasicTextField(
            value = viewModel.textTitle,
            onValueChange = { viewModel.textTitle = it },
            textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold),
            cursorBrush = SolidColor(themeData.accentColor),
            decorationBox = { innerTextField ->
                if (viewModel.textTitle.isEmpty()) {
                    Text("Memory Title (Optional)", style = MaterialTheme.typography.bodyMedium, color = TextGray)
                }
                innerTextField()
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                .padding(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Content Input
        BasicTextField(
            value = viewModel.textContent,
            onValueChange = { viewModel.textContent = it },
            textStyle = TextStyle(color = TextSilver, fontSize = 14.sp),
            cursorBrush = SolidColor(themeData.accentColor),
            decorationBox = { innerTextField ->
                if (viewModel.textContent.isEmpty()) {
                    Text("Capture your thoughts here...", style = MaterialTheme.typography.bodyMedium, color = TextGray)
                }
                innerTextField()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                .padding(12.dp)
        )

        // Embed Atmospheric Metadata Form
        AtmosphericMetadataForm(
            weather = viewModel.entryWeather,
            onWeatherChange = { viewModel.entryWeather = it },
            locationName = viewModel.entryLocationName,
            onLocationChange = { viewModel.entryLocationName = it },
            isDeletable = viewModel.entryIsDeletable,
            onDeletableChange = { viewModel.entryIsDeletable = it },
            valPeace = viewModel.valPeace,
            onPeaceChange = { viewModel.valPeace = it },
            valWarmth = viewModel.valWarmth,
            onWarmthChange = { viewModel.valWarmth = it },
            valAnxiety = viewModel.valAnxiety,
            onAnxietyChange = { viewModel.valAnxiety = it },
            valMelancholy = viewModel.valMelancholy,
            onMelancholyChange = { viewModel.valMelancholy = it },
            themeData = themeData
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text(text = "CANCEL", color = TextGray, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onSave,
                enabled = viewModel.textContent.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.neonGlow(color = if (viewModel.textContent.isNotBlank()) themeData.accentColor else Color.Gray, cornerRadius = 20.dp)
            ) {
                Text(text = "ARCHIVE", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun VoiceRecordingForm(
    themeData: CustomThemeData,
    viewModel: RoomDetailViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val recordState by viewModel.audioRecorder.state.collectAsState()
    val durationMs by viewModel.audioRecorder.durationMs.collectAsState()
    val amplitudes by viewModel.recordingAmplitudes.collectAsState()

    val formattedDuration = remember(durationMs) {
        val totalSeconds = durationMs / 1000
        "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "ECHO VOICE RECORDING",
            style = MaterialTheme.typography.labelSmall,
            color = themeData.accentColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(14.dp))

        BasicTextField(
            value = viewModel.textTitle,
            onValueChange = { viewModel.textTitle = it },
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center),
            cursorBrush = SolidColor(themeData.accentColor),
            decorationBox = { innerTextField ->
                if (viewModel.textTitle.isEmpty()) {
                    Text("Name this recording (Optional)", style = MaterialTheme.typography.bodyMedium, color = TextGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                innerTextField()
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic Waveform Visualizer
        WaveformVisualizer(
            amplitudes = amplitudes,
            color = themeData.accentColor,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recording Dot and Counter
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color.Red, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formattedDuration,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontSize = 14.sp
            )
        }

        // Embed Atmospheric Metadata Form
        AtmosphericMetadataForm(
            weather = viewModel.entryWeather,
            onWeatherChange = { viewModel.entryWeather = it },
            locationName = viewModel.entryLocationName,
            onLocationChange = { viewModel.entryLocationName = it },
            isDeletable = viewModel.entryIsDeletable,
            onDeletableChange = { viewModel.entryIsDeletable = it },
            valPeace = viewModel.valPeace,
            onPeaceChange = { viewModel.valPeace = it },
            valWarmth = viewModel.valWarmth,
            onWarmthChange = { viewModel.valWarmth = it },
            valAnxiety = viewModel.valAnxiety,
            onAnxietyChange = { viewModel.valAnxiety = it },
            valMelancholy = viewModel.valMelancholy,
            onMelancholyChange = { viewModel.valMelancholy = it },
            themeData = themeData
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0x0CFFFFFF), CircleShape)
                    .border(1.dp, GlassBorder, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
            }

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .height(44.dp)
                    .neonGlow(color = themeData.accentColor, cornerRadius = 22.dp)
            ) {
                Text(text = "STOP & ARCHIVE", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ImageCaptureForm(
    viewModel: RoomDetailViewModel,
    themeData: CustomThemeData,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "ARCHIVE CAPTURED IMAGE",
            style = MaterialTheme.typography.labelSmall,
            color = themeData.accentColor,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        val uri = viewModel.selectedImageUri
        if (uri != null) {
            AsyncImage(
                model = uri,
                contentDescription = "Image preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title Input
        BasicTextField(
            value = viewModel.imageTitle,
            onValueChange = { viewModel.imageTitle = it },
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            cursorBrush = SolidColor(themeData.accentColor),
            decorationBox = { innerTextField ->
                if (viewModel.imageTitle.isEmpty()) {
                    Text("Label this capture (e.g. Rainy cafe window)", style = MaterialTheme.typography.bodyMedium, color = TextGray)
                }
                innerTextField()
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        )

        // Embed Atmospheric Metadata Form
        AtmosphericMetadataForm(
            weather = viewModel.entryWeather,
            onWeatherChange = { viewModel.entryWeather = it },
            locationName = viewModel.entryLocationName,
            onLocationChange = { viewModel.entryLocationName = it },
            isDeletable = viewModel.entryIsDeletable,
            onDeletableChange = { viewModel.entryIsDeletable = it },
            valPeace = viewModel.valPeace,
            onPeaceChange = { viewModel.valPeace = it },
            valWarmth = viewModel.valWarmth,
            onWarmthChange = { viewModel.valWarmth = it },
            valAnxiety = viewModel.valAnxiety,
            onAnxietyChange = { viewModel.valAnxiety = it },
            valMelancholy = viewModel.valMelancholy,
            onMelancholyChange = { viewModel.valMelancholy = it },
            themeData = themeData
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text(text = "CANCEL", color = TextGray, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.neonGlow(color = themeData.accentColor, cornerRadius = 20.dp)
            ) {
                Text(text = "ARCHIVE IMAGE", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun AtmosphericMetadataForm(
    weather: String,
    onWeatherChange: (String) -> Unit,
    locationName: String,
    onLocationChange: (String) -> Unit,
    isDeletable: Boolean,
    onDeletableChange: (Boolean) -> Unit,
    valPeace: Float,
    onPeaceChange: (Float) -> Unit,
    valWarmth: Float,
    onWarmthChange: (Float) -> Unit,
    valAnxiety: Float,
    onAnxietyChange: (Float) -> Unit,
    valMelancholy: Float,
    onMelancholyChange: (Float) -> Unit,
    themeData: CustomThemeData
) {
    val weatherPresets = listOf("Clear", "Rainy", "Nebula", "Cyber Glow", "Midnight Fog", "Sunny Sunset")

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ATMOSPHERIC IMPRINT",
            style = MaterialTheme.typography.labelSmall,
            color = themeData.accentColor,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Weather Preset Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            weatherPresets.forEach { preset ->
                val isSelected = weather == preset
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) themeData.accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) themeData.accentColor else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onWeatherChange(preset) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset,
                        color = if (isSelected) Color.White else TextSilver,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Location Text Field
        BasicTextField(
            value = locationName,
            onValueChange = onLocationChange,
            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
            cursorBrush = SolidColor(themeData.accentColor),
            decorationBox = { innerTextField ->
                if (locationName.isEmpty()) {
                    Text("Atmospheric Location (e.g. Cozy Cafe, Rainy Window)", style = MaterialTheme.typography.bodyMedium, color = TextGray, fontSize = 13.sp)
                }
                innerTextField()
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Deletability switch (Temporal Drift resistance)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .clickable { onDeletableChange(!isDeletable) }
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🔒 TEMPORAL DRIFT ANCHOR",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isDeletable) "This capsule can be deleted later." else "Permanent anchor: prevent capsule deletion.",
                    color = TextGray,
                    fontSize = 10.sp
                )
            }
            // Simple Custom Toggle
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!isDeletable) themeData.accentColor else Color.White.copy(alpha = 0.15f))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .align(if (!isDeletable) Alignment.CenterEnd else Alignment.CenterStart)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Emotion sliders
        EmotionSliders(
            valPeace = valPeace,
            onPeaceChange = onPeaceChange,
            valWarmth = valWarmth,
            onWarmthChange = onWarmthChange,
            valAnxiety = valAnxiety,
            onAnxietyChange = onAnxietyChange,
            valMelancholy = valMelancholy,
            onMelancholyChange = onMelancholyChange
        )
    }
}

@Composable
private fun BiometricLockOverlay(
    room: RoomEntity,
    themeData: CustomThemeData,
    onAuthenticate: () -> Unit,
    biometricError: String?,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        onAuthenticate()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .neonGlow(color = themeData.glowColor, cornerRadius = 60.dp)
                .background(Color.White.copy(alpha = 0.03f), CircleShape)
                .border(1.dp, themeData.accentColor.copy(alpha = 0.3f), CircleShape)
                .clickable { onAuthenticate() }
        ) {
            Text(text = "⚛️", fontSize = 48.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "C H A M B E R   L O C K E D",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "This memory space is encrypted. Authenticate to decrypt the contents of ${room.title}.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSilver,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        if (biometricError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = biometricError,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .height(44.dp)
                    .border(1.dp, GlassBorder, RoundedCornerShape(22.dp))
            ) {
                Text(
                    text = "DEPART",
                    color = TextSilver,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Button(
                onClick = onAuthenticate,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .height(44.dp)
                    .neonGlow(color = themeData.accentColor, cornerRadius = 22.dp)
            ) {
                Text(
                    text = "DECRYPT",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
