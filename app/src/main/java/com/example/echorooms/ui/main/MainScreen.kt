package com.example.echorooms.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import com.example.echorooms.CreateRoom
import com.example.echorooms.EchoRoomsApplication
import com.example.echorooms.RoomDetail
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as EchoRoomsApplication
    val viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(app.roomRepository) }

    val state by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()

    AmbientBackground {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onItemClick(CreateRoom) },
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .neonGlow(color = NeonPink, cornerRadius = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Room",
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .statusBarsPadding()
            ) {
                // Cinematic App Bar Title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "E C H O  R O O M S",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Futuristic Emotional Memory Capsule",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray
                    )
                }

                // Search Bar + Favorites Filter Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    // Glassmorphic Search Input
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .glassmorphic(cornerRadius = 24.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            textStyle = TextStyle(
                                color = TextSilver,
                                fontSize = 15.sp,
                                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily
                            ),
                            cursorBrush = SolidColor(NeonCyan),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search memory spaces...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextGray
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Glassmorphic Favorite Button
                    IconButton(
                        onClick = { viewModel.showOnlyFavorites.value = !showOnlyFavorites },
                        modifier = Modifier
                            .size(48.dp)
                            .glassmorphic(
                                cornerRadius = 24.dp,
                                backgroundColor = if (showOnlyFavorites) Color(0x20FFFF00) else Color(0x0CFFFFFF),
                                borderColor = if (showOnlyFavorites) NeonPink.copy(alpha = 0.5f) else GlassBorder
                            )
                    ) {
                        Icon(
                            imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorites Filter",
                            tint = if (showOnlyFavorites) NeonPink else TextGray
                        )
                    }
                }

                // Grid Content
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    when (val s = state) {
                        is MainScreenUiState.Loading -> {
                            CircularProgressIndicator(color = NeonCyan)
                        }
                        is MainScreenUiState.Error -> {
                            Text(
                                text = "Failed to load rooms: ${s.throwable.localizedMessage}",
                                color = NeonPink,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                        is MainScreenUiState.Success -> {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = s.rooms.isEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                MainScreenEmptyState()
                            }

                            androidx.compose.animation.AnimatedVisibility(
                                visible = s.rooms.isNotEmpty(),
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(s.rooms, key = { it.id }) { room ->
                                        RoomCard(
                                            room = room,
                                            onClick = { onItemClick(RoomDetail(room.id)) },
                                            onFavoriteToggle = { viewModel.toggleFavorite(room) },
                                            onDelete = { viewModel.deleteRoom(room) }
                                        )
                                    }
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
private fun MainScreenEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Glowing decorative graphic (using basic canvas blur)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(100.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .blur(20.dp)
                    .background(NeonPink.copy(alpha = 0.4f), CircleShape)
            )
            Text(
                text = "🌌",
                fontSize = 42.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Digital Archive Vacant",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No memory capsules have been preserved here yet. Create your first emotional chamber.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth(0.85f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoomCard(
    room: RoomEntity,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = room.getMoodTheme()
    var showDeleteOption by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .neonGlow(color = theme.glow(), cornerRadius = 16.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteOption = !showDeleteOption }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Emoji and Favorite Button
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
                    Text(text = room.iconEmoji, fontSize = 18.sp)
                }

                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (room.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (room.isFavorite) NeonPink else TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cover Image Thumbnail (if exists)
            if (!room.coverImagePath.isNullOrEmpty()) {
                val imageFile = File(room.coverImagePath)
                if (imageFile.exists()) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = "Cover Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Room Title
            Text(
                text = room.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Mood description tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(theme.glow(), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = theme.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.accent()
                )
            }

            // Room Description (Short snippet)
            if (room.description.isNotEmpty()) {
                Text(
                    text = room.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time Capsule / Date text
            val formattedDate = remember(room.createdAt) {
                val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                sdf.format(Date(room.createdAt))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGray
                )

                if (room.isTimeCapsule) {
                    Text(
                        text = if (room.isLocked()) "🔒 LOCKED" else "🔓 UNLOCKED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (room.isLocked()) NeonPink else NeonCyan,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Overlay Delete Option when Long Pressed
        AnimatedVisibility(
            visible = showDeleteOption,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(Color(0xEC09090C))
                    .combinedClickable(
                        onClick = { showDeleteOption = false },
                        onLongClick = { showDeleteOption = false }
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            onDelete()
                            showDeleteOption = false
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0x26FF0080), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Room",
                            tint = NeonPink
                        )
                    }
                }
            }
        }
    }
}
