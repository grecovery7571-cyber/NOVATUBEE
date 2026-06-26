package com.example.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import com.example.ui.theme.*
import com.example.R
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.components.ExoVideoPlayer
import com.example.ui.components.YouTubeNativePlayer
import com.example.viewmodel.TubeViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: TubeViewModel,
    modifier: Modifier = Modifier,
    onEnterPiP: () -> Unit = {}
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("home") }
    val currentVideo by viewModel.currentPlayingVideo.collectAsState()
    val isFloatingActive by viewModel.isFloatingWindowActive.collectAsState()
    
    // Manage detail panel expansion when not floating
    var isDetailPanelExpanded by remember { mutableStateOf(false) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    var playlistVideoToSave by remember { mutableStateOf<VideoItem?>(null) }

    // If a new video starts and is NOT floating, automatically expand detail panel
    LaunchedEffect(currentVideo) {
        if (currentVideo != null && !isFloatingActive) {
            isDetailPanelExpanded = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFF0F0F0F),
        bottomBar = {
            // Under PiP mode, hide systems bottom UI
            if (currentVideo == null || isFloatingActive || !isDetailPanelExpanded) {
                Column {
                    HorizontalDivider(color = SlateBorder, thickness = 1.dp)
                    NavigationBar(
                        containerColor = Color(0xFF0F0F0F),
                        tonalElevation = 0.dp
                    ) {
                        val navItemColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Slate400,
                            selectedTextColor = Color.White,
                            unselectedTextColor = Slate400,
                            indicatorColor = Color(0xFF272727)
                        )
                        NavigationBarItem(
                            selected = currentTab == "home",
                            onClick = { currentTab = "home" },
                            icon = { Icon(if (currentTab == "home") Icons.Filled.Home else Icons.Filled.Home, contentDescription = "Home", tint = if (currentTab == "home") Color.White else Slate400) },
                            label = { Text("Home", fontSize = 11.sp, fontWeight = if (currentTab == "home") FontWeight.Bold else FontWeight.Normal) },
                            colors = navItemColors
                        )
                        NavigationBarItem(
                            selected = currentTab == "search",
                            onClick = { currentTab = "search" },
                            icon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = if (currentTab == "search") Color.White else Slate400) },
                            label = { Text("Search", fontSize = 11.sp, fontWeight = if (currentTab == "search") FontWeight.Bold else FontWeight.Normal) },
                            colors = navItemColors
                        )
                        NavigationBarItem(
                            selected = currentTab == "library",
                            onClick = { currentTab = "library" },
                            icon = { Icon(Icons.Filled.VideoLibrary, contentDescription = "Library", tint = if (currentTab == "library") Color.White else Slate400) },
                            label = { Text("Library", fontSize = 11.sp, fontWeight = if (currentTab == "library") FontWeight.Bold else FontWeight.Normal) },
                            colors = navItemColors
                        )
                        NavigationBarItem(
                            selected = currentTab == "settings",
                            onClick = { currentTab = "settings" },
                            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = if (currentTab == "settings") Color.White else Slate400) },
                            label = { Text("Settings", fontSize = 11.sp, fontWeight = if (currentTab == "settings") FontWeight.Bold else FontWeight.Normal) },
                            colors = navItemColors
                        )
                    }
                }
            }
        },
        topBar = {
            if (currentVideo == null || isFloatingActive || !isDetailPanelExpanded) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = R.drawable.novatube_logo,
                                contentDescription = "NOVATUBE Logo",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Row {
                                Text(
                                    "NOVA",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    color = Color.White
                                )
                                Text(
                                    "TUBE",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    color = RedBrand
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF0F0F0F)
                    ),
                    actions = {
                        // Sleep count status
                        val isTimerRunning by viewModel.isSleepTimerRunning.collectAsState()
                        val remainingSecs by viewModel.sleepTimerRemainingSeconds.collectAsState()
                        if (isTimerRunning) {
                            val mins = remainingSecs / 60
                            val secs = remainingSecs % 60
                            AssistChip(
                                onClick = { currentTab = "settings" },
                                label = { Text(String.format(Locale.getDefault(), "%02d:%02d", mins, secs), color = Color.Red, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Filled.Timer, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp)) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Notification icon
                        val notifications by viewModel.notifications.collectAsState()
                        val unreadCount = notifications.count { !it.isRead }
                        IconButton(onClick = { showNotificationSheet = true }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = RedBrand) {
                                            Text(unreadCount.toString(), color = Color.White, fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Notifications Center",
                                    tint = Slate100
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs router with fade-through animations
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) togetherWith 
                    fadeOut(animationSpec = androidx.compose.animation.core.tween(220))
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    "home" -> HomeFeedView(
                        viewModel = viewModel,
                        onVideoSelected = { video ->
                            viewModel.setPlayingVideo(video, makeFloating = false)
                            isDetailPanelExpanded = true
                        }
                    )
                    "search" -> SearchView(
                        viewModel = viewModel,
                        onVideoSelected = { video ->
                            viewModel.setPlayingVideo(video, makeFloating = false)
                            isDetailPanelExpanded = true
                        }
                    )
                    "library" -> LibraryView(
                        viewModel = viewModel,
                        onVideoSelected = { video ->
                            viewModel.setPlayingVideo(video, makeFloating = false)
                            isDetailPanelExpanded = true
                        },
                        onNavigateToTab = { tab ->
                            currentTab = tab
                        }
                    )
                    "settings" -> SettingsView(
                        viewModel = viewModel,
                        onEnterPiP = onEnterPiP
                    )
                }
            }

            // Expanded Video details full sheet with Slide-Up Swipe animation!
            AnimatedVisibility(
                visible = currentVideo != null && !isFloatingActive && isDetailPanelExpanded,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                if (currentVideo != null) {
                    VideoDetailsExpandedView(
                        viewModel = viewModel,
                        video = currentVideo!!,
                        onMinimizeToFloating = {
                            viewModel.toggleFloatingPlayer(true)
                            isDetailPanelExpanded = false
                        },
                        onDismiss = {
                            viewModel.setPlayingVideo(null)
                            isDetailPanelExpanded = false
                        },
                        onAddToPlaylist = { videoItem ->
                            playlistVideoToSave = videoItem
                        }
                    )
                }
            }

            // In-App INTERACTIVE DRAGGABLE RESIZABLE FLOATING OVERLAY WINDOW PLAYER
            if (currentVideo != null && isFloatingActive) {
                FloatingOverlayPlayer(
                    viewModel = viewModel,
                    video = currentVideo!!,
                    onRestore = {
                        viewModel.toggleFloatingPlayer(false)
                        isDetailPanelExpanded = true
                    }
                )
            }

            // --- NOTIFICATION CENTER MODAL OVERLAY ---
            if (showNotificationSheet) {
                val notifications by viewModel.notifications.collectAsState()
                AlertDialog(
                    onDismissRequest = { showNotificationSheet = false },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notifications", color = Slate100, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            if (notifications.isNotEmpty()) {
                                TextButton(onClick = { viewModel.clearNotifications() }) {
                                    Text("Clear All", color = RedBrand, fontSize = 12.sp)
                                }
                            }
                        }
                    },
                    text = {
                        if (notifications.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.NotificationsOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No new notifications", color = Slate400, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Subscribe to channels to trigger real-time simulated uploads!",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(notifications) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (item.isRead) DarkSurface else Color(0xFF1E1E1E))
                                            .border(1.dp, if (item.isRead) SlateBorder else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                            .clickable {
                                                viewModel.markNotificationAsRead(item.id)
                                                val targetVideo = VideoItem(
                                                    videoId = item.videoId,
                                                    title = item.message,
                                                    description = item.message,
                                                    channelTitle = item.channelTitle,
                                                    thumbnailUrl = item.thumbnailUrl,
                                                    publishedAt = "Recently",
                                                    viewCount = "1.5M views",
                                                    duration = "15:42"
                                                )
                                                viewModel.setPlayingVideo(targetVideo, makeFloating = false)
                                                isDetailPanelExpanded = true
                                                showNotificationSheet = false
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = item.thumbnailUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.title, color = Slate100, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(item.message, color = Slate400, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                        if (!item.isRead) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(start = 6.dp)
                                                    .size(8.dp)
                                                    .background(RedBrand, CircleShape)
                                                    .align(Alignment.CenterVertically)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showNotificationSheet = false },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Close", color = Slate100, fontSize = 12.sp)
                        }
                    },
                    containerColor = Color(0xFF121212)
                )
            }

            // --- SAVE TO PLAYLIST DIALOG OVERLAY ---
            if (playlistVideoToSave != null) {
                val playlists by viewModel.playlists.collectAsState()
                var newPlaylistName by remember { mutableStateOf("") }
                var showCreateInline by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { playlistVideoToSave = null },
                    title = {
                        Text("Save to Playlist", color = Slate100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (playlists.isEmpty()) {
                                Text("No custom playlists created yet.", color = Slate400, fontSize = 13.sp)
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(playlists) { playlist ->
                                        val videosFlow = remember(playlist.id) { viewModel.getVideosForPlaylist(playlist.id) }
                                        val videos by videosFlow.collectAsState(initial = emptyList())
                                        val alreadyAdded = videos.any { it.videoId == playlistVideoToSave!!.videoId }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(DarkSurface)
                                                .clickable {
                                                    if (alreadyAdded) {
                                                        viewModel.removeVideoFromPlaylist(playlist.id, playlistVideoToSave!!.videoId)
                                                        Toast.makeText(context, "Removed from ${playlist.name}", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        viewModel.addVideoToPlaylist(playlist.id, playlistVideoToSave!!)
                                                        Toast.makeText(context, "Saved to ${playlist.name}!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(playlist.name, color = Slate100, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                            Checkbox(
                                                checked = alreadyAdded,
                                                onCheckedChange = null,
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = MaterialTheme.colorScheme.primary,
                                                    checkmarkColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = SlateBorder, thickness = 1.dp)

                            if (!showCreateInline) {
                                TextButton(
                                    onClick = { showCreateInline = true },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Create New Playlist", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(
                                        value = newPlaylistName,
                                        onValueChange = { newPlaylistName = it },
                                        label = { Text("Playlist Name", color = Slate500, fontSize = 12.sp) },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Slate400,
                                            unfocusedBorderColor = SlateBorder,
                                            focusedTextColor = Slate100,
                                            unfocusedTextColor = Slate100
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { showCreateInline = false }) {
                                            Text("Cancel", color = Slate400, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                if (newPlaylistName.isNotBlank()) {
                                                    viewModel.createPlaylist(newPlaylistName)
                                                    newPlaylistName = ""
                                                    showCreateInline = false
                                                    Toast.makeText(context, "Playlist created!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Create", color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { playlistVideoToSave = null },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done", color = Slate100, fontSize = 12.sp)
                        }
                    },
                    containerColor = Color(0xFF121212)
                )
            }
        }
    }
}

// ---------------- HOME FEED ----------------
@Composable
fun HomeFeedView(
    viewModel: TubeViewModel,
    onVideoSelected: (VideoItem) -> Unit
) {
    val homeVideos by viewModel.homeVideos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    if (isLoading && homeVideos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Featured Videos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    TextButton(onClick = { viewModel.refreshHomeFeed() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refresh")
                    }
                }
            }

            if (homeVideos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No video feeds found. Check Internet / Custom API Key.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                items(homeVideos) { video ->
                    VideoListItemCard(video = video, onClick = { onVideoSelected(video) })
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

// ---------------- SEARCH VIEW ----------------
@Composable
fun SearchView(
    viewModel: TubeViewModel,
    onVideoSelected: (VideoItem) -> Unit
) {
    val videos by viewModel.videos.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchInput,
            onValueChange = { searchInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            placeholder = { Text("Search topics, creators, or shorts...", color = Slate500, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Slate400) },
            trailingIcon = {
                if (searchInput.isNotEmpty()) {
                    IconButton(onClick = { searchInput = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Slate400)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (searchInput.isNotBlank()) {
                    viewModel.searchVideos(searchInput)
                    keyboardController?.hide()
                }
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Slate400,
                unfocusedBorderColor = SlateBorder,
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant,
                focusedTextColor = Slate100,
                unfocusedTextColor = Slate100
            ),
            shape = CircleShape
        )

        // Loading
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (videos.isEmpty()) {
            // Show recent search queries list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (searchHistory.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent Searches", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Text("Clear All", color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(searchHistory) { historyItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        searchInput = historyItem.query
                                        viewModel.searchVideos(historyItem.query)
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(historyItem.query, color = Color.White, fontSize = 15.sp)
                                }
                                IconButton(
                                    modifier = Modifier.size(24.dp),
                                    onClick = { viewModel.deleteHistoryItem(historyItem.query) }
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Tv, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Discover custom clips & songs instantly", color = Color.Gray, fontSize = 13.sp)
                            Text("Defaults: 'Lofi', 'Nature', 'Gaming', 'Tech'", color = Color.DarkGray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        } else {
            // Results Feed
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(videos) { video ->
                    VideoListItemCard(video = video, onClick = { onVideoSelected(video) })
                }
            }
        }
    }
}

// ---------------- LIBRARY / SAVED PLAYLISTS ----------------
@Composable
fun LibraryView(
    viewModel: TubeViewModel,
    onVideoSelected: (VideoItem) -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    val context = LocalContext.current
    val favorites by viewModel.favorites.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState(initial = emptyList())
    val playlists by viewModel.playlists.collectAsState(initial = emptyList())

    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userAvatarId by viewModel.userAvatarId.collectAsState()

    // Create playlist inline state
    var isCreatingPlaylist by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    
    // Expanded playlist state (null means none, otherwise contains playlistId)
    var expandedPlaylistId by remember { mutableStateOf<Int?>(null) }

    val avatarGradients = listOf(
        listOf(Color(0xFFE91E63), Color(0xFF9C27B0)),
        listOf(Color(0xFF2196F3), Color(0xFF00BCD4)),
        listOf(Color(0xFF4CAF50), Color(0xFF8BC34A)),
        listOf(Color(0xFFFF9800), Color(0xFFFFC107)),
        listOf(Color(0xFF607D8B), Color(0xFF9E9E9E)),
        listOf(Color(0xFF009688), Color(0xFF3F51B5))
    )
    val chosenGradient = avatarGradients.getOrElse(userAvatarId) { avatarGradients.first() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. USER ACCOUNT BRIEF CARD ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E1E))
                    .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(chosenGradient)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(Locale.getDefault()),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(userName, color = Slate100, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        val isPremium by viewModel.isUserPremium.collectAsState()
                        if (isPremium) {
                            Text(
                                "PREMIUM 💎",
                                color = Color(0xFFFFD700),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(userEmail, color = Slate500, fontSize = 12.sp)
                }
                IconButton(onClick = { onNavigateToTab("settings") }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = Slate400)
                }
            }
        }

        // --- 2. SUBSCRIBED CHANNELS ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Subscriptions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (subscriptions.isEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Subscriptions, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("No subscriptions yet", color = Slate100, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Subscribe to your favorite channels on video details!", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(subscriptions) { sub ->
                            val channelColor = remember(sub.channelTitle) {
                                val hash = sub.channelTitle.hashCode()
                                avatarGradients[Math.abs(hash) % avatarGradients.size]
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(72.dp)
                                    .clickable {
                                        viewModel.searchVideos(sub.channelTitle)
                                        onNavigateToTab("search")
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(androidx.compose.ui.graphics.Brush.linearGradient(channelColor)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sub.channelTitle.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    sub.channelTitle,
                                    color = Slate100,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. CUSTOM PLAYLISTS SECTION ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "My Custom Playlists",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate100
                    )
                    IconButton(onClick = { isCreatingPlaylist = !isCreatingPlaylist }) {
                        Icon(
                            imageVector = if (isCreatingPlaylist) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = "Create Playlist",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (isCreatingPlaylist) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newPlaylistName,
                            onValueChange = { newPlaylistName = it },
                            placeholder = { Text("Playlist name...", color = Slate500, fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Slate400,
                                unfocusedBorderColor = SlateBorder,
                                focusedTextColor = Slate100,
                                unfocusedTextColor = Slate100
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                if (newPlaylistName.isNotBlank()) {
                                    viewModel.createPlaylist(newPlaylistName)
                                    newPlaylistName = ""
                                    isCreatingPlaylist = false
                                    Toast.makeText(context, "Playlist created successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Create", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }

                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.PlaylistPlay, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Create your first custom playlist!", color = Slate400, fontSize = 12.sp)
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        playlists.forEach { playlist ->
                            val isExpanded = expandedPlaylistId == playlist.id
                            val videosFlow = remember(playlist.id) { viewModel.getVideosForPlaylist(playlist.id) }
                            val videos by videosFlow.collectAsState(initial = emptyList())

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        expandedPlaylistId = if (isExpanded) null else playlist.id
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.DarkGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (videos.isNotEmpty()) {
                                                AsyncImage(
                                                    model = videos.first().thumbnailUrl,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(Icons.Filled.PlaylistPlay, contentDescription = null, tint = Color.White)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(playlist.name, color = Slate100, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("${videos.size} videos", color = Slate500, fontSize = 11.sp)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            viewModel.deletePlaylist(playlist)
                                            Toast.makeText(context, "Playlist Deleted", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete Playlist", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                            contentDescription = null,
                                            tint = Slate400
                                        )
                                    }
                                }

                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f), thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (videos.isEmpty()) {
                                        Text(
                                            "This playlist is empty. Open a video and tap 'Add' to save!",
                                            color = Color.Gray,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    } else {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            videos.forEach { pVideo ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFF161616))
                                                        .clickable {
                                                            // Play it
                                                            val vItem = VideoItem(
                                                                videoId = pVideo.videoId,
                                                                title = pVideo.title,
                                                                description = "",
                                                                thumbnailUrl = pVideo.thumbnailUrl,
                                                                channelTitle = pVideo.channelTitle,
                                                                duration = pVideo.duration
                                                            )
                                                            onVideoSelected(vItem)
                                                        }
                                                        .padding(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    AsyncImage(
                                                        model = pVideo.thumbnailUrl,
                                                        contentDescription = null,
                                                        modifier = Modifier
                                                            .width(72.dp)
                                                            .height(42.dp)
                                                            .clip(RoundedCornerShape(4.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(pVideo.title, color = Slate100, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        Text(pVideo.channelTitle, color = Slate500, fontSize = 10.sp)
                                                    }
                                                    IconButton(onClick = {
                                                        viewModel.removeVideoFromPlaylist(playlist.id, pVideo.videoId)
                                                        Toast.makeText(context, "Removed video", Toast.LENGTH_SHORT).show()
                                                    }) {
                                                        Icon(Icons.Filled.Close, contentDescription = "Remove video", tint = Color.Gray, modifier = Modifier.size(16.dp))
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
            }
        }

        // --- 4. SAVED VIDEOS (FAVORITES) ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Liked Videos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate100,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (favorites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.FavoriteBorder, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("No liked videos yet.", color = Slate400, fontSize = 12.sp)
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        favorites.forEach { fav ->
                            val videoItem = fav.toVideoItem()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onVideoSelected(videoItem) }
                                    .background(DarkSurface, RoundedCornerShape(12.dp))
                                    .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = videoItem.thumbnailUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(96.dp)
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        videoItem.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        videoItem.channelTitle,
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }

                                IconButton(onClick = { viewModel.toggleFavorite(videoItem) }) {
                                    Icon(Icons.Filled.Favorite, contentDescription = "Remove", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SETTINGS & CONFIGURATION ----------------
@Composable
fun SettingsView(
    viewModel: TubeViewModel,
    onEnterPiP: () -> Unit
) {
    val savedKey by viewModel.apiKey.collectAsState()
    var inputKey by remember { mutableStateOf(savedKey) }
    var keyVisible by remember { mutableStateOf(false) }

    val isTimerRunning by viewModel.isSleepTimerRunning.collectAsState()
    val remainingSecs by viewModel.sleepTimerRemainingSeconds.collectAsState()
    val isPiPActive by viewModel.isPiPEnabled.collectAsState()

    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userAvatarId by viewModel.userAvatarId.collectAsState()
    val isPremium by viewModel.isUserPremium.collectAsState()

    val isTestingApi by viewModel.isTestingApi.collectAsState()
    val apiTestStatus by viewModel.apiTestStatus.collectAsState()

    var showProfileEditDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(savedKey) {
        inputKey = savedKey
    }

    val avatarGradients = listOf(
        listOf(Color(0xFFE91E63), Color(0xFF9C27B0)),
        listOf(Color(0xFF2196F3), Color(0xFF00BCD4)),
        listOf(Color(0xFF4CAF50), Color(0xFF8BC34A)),
        listOf(Color(0xFFFF9800), Color(0xFFFFC107)),
        listOf(Color(0xFF607D8B), Color(0xFF9E9E9E)),
        listOf(Color(0xFF009688), Color(0xFF3F51B5))
    )
    val chosenGradient = avatarGradients.getOrElse(userAvatarId) { avatarGradients.first() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. USER ACCOUNT / PROFILE SECTION ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "My NovaTube Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Slate100,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1E1E))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    androidx.compose.ui.graphics.Brush.linearGradient(chosenGradient)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userName,
                                    color = Slate100,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isPremium) {
                                    Text(
                                        "PREMIUM 💎",
                                        color = Color(0xFFFFD700),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(userEmail, color = Slate400, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showProfileEditDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.ManageAccounts, contentDescription = null, tint = Slate100)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Account Profile", color = Slate100)
                    }
                }
            }
        }

        // --- 2. YT API CREDENTIALS & CONNECTION CENTRE ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "YouTube API Configuration",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Slate100,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Directly fetch original YouTube search results, live details, comments, and channels. Insert your custom Google API Key or use the built-in sandbox key.",
                        color = Slate400,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Google Cloud API Key", color = Slate500) },
                        singleLine = true,
                        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    if (keyVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle Key View",
                                    tint = Slate400
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Slate400,
                            unfocusedBorderColor = SlateBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // API TEST STATUS LOGGER BANNER
                    if (isTestingApi) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E1E1E))
                                .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Testing API connection, verifying payload...", color = Slate400, fontSize = 11.sp)
                        }
                    } else {
                        val currentTestStatus = apiTestStatus
                        if (!currentTestStatus.isNullOrBlank()) {
                            val isSuccess = currentTestStatus.contains("SUCCESS", ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSuccess) Color(0xFF0F2D1F) else Color(0xFF3B1519))
                                    .border(1.dp, if (isSuccess) Color(0xFF1B5E20) else Color(0xFFB71C1C), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                    contentDescription = null,
                                    tint = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = currentTestStatus,
                                    color = if (isSuccess) Color(0xFFC8E6C9) else Color(0xFFFFCDD2),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Reset Key
                        Button(
                            onClick = {
                                viewModel.removeApiKey()
                                Toast.makeText(context, "API key restored to default built-in credentials!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Restore, contentDescription = null, tint = Slate100, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", color = Slate100, fontSize = 12.sp)
                        }

                        // Save & Test
                        Button(
                            onClick = {
                                viewModel.saveApiKey(inputKey)
                                viewModel.testApiKeyConnection(inputKey)
                            },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Power, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Connection", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- 3. SLEEP MODE TIMER ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Sleep Mode Timer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Slate100,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Need sleep or study constraints? Automatically pause audio and video streams after a specific set duration.",
                        color = Slate400,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (isTimerRunning) {
                        val mins = remainingSecs / 60
                        val secs = remainingSecs % 60
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Remaining Clock: " + String.format(Locale.getDefault(), "%02d:%02d", mins, secs),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Button(
                                onClick = { viewModel.stopSleepTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel Timer", color = Slate100)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 15, 30, 60).forEach { mins ->
                                Button(
                                    onClick = {
                                        viewModel.startSleepTimer(mins)
                                        Toast.makeText(context, "Sleep countdown started for $mins minutes", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("${mins}m", color = Slate100, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. AUTO PICTURE IN PICTURE ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Auto Picture-in-Picture",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate100
                        )
                        Text(
                            "Enable to enter PiP mode automatically when minimizing the application.",
                            color = Slate400,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = isPiPActive,
                        onCheckedChange = { viewModel.isPiPEnabled.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = DarkSurfaceVariant
                        )
                    )
                }
            }
        }

        // Pip quick-test trigger
        item {
            Button(
                onClick = onEnterPiP,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.PictureInPicture, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Picture-In-Picture View", color = MaterialTheme.colorScheme.primary)
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- INTERACTIVE PROFILE EDIT DIALOG ---
    if (showProfileEditDialog) {
        var tempName by remember { mutableStateOf(userName) }
        var tempEmail by remember { mutableStateOf(userEmail) }
        var tempPremium by remember { mutableStateOf(isPremium) }
        var tempAvatarId by remember { mutableStateOf(userAvatarId) }

        AlertDialog(
            onDismissRequest = { showProfileEditDialog = false },
            title = {
                Text("Edit Account Profile", color = Slate100, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar preset selection
                    Text("Choose Avatar Style", color = Slate400, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        avatarGradients.forEachIndexed { idx, colors ->
                            val isSelected = tempAvatarId == idx
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Brush.linearGradient(colors))
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { tempAvatarId = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Profile Name", color = Slate500) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Slate400,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = tempEmail,
                        onValueChange = { tempEmail = it },
                        label = { Text("Profile Email", color = Slate500) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Slate400,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = Slate100,
                            unfocusedTextColor = Slate100
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Premium Active Status 💎", color = Slate100, fontSize = 14.sp)
                        Switch(
                            checked = tempPremium,
                            onCheckedChange = { tempPremium = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = Slate400,
                                uncheckedTrackColor = DarkSurfaceVariant
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank() && tempEmail.isNotBlank()) {
                            viewModel.updateProfile(tempName, tempEmail, tempAvatarId, tempPremium)
                            showProfileEditDialog = false
                            Toast.makeText(context, "Account profile updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Name and Email cannot be empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Profile", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileEditDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            },
            containerColor = Color(0xFF121212)
        )
    }
}


// ---------------- DRAGGABLE FLOATING PLAYER COMPONENT ----------------
@Composable
fun FloatingOverlayPlayer(
    viewModel: TubeViewModel,
    video: VideoItem,
    onRestore: () -> Unit
) {
    val floatingX by viewModel.floatingX.collectAsState()
    val floatingY by viewModel.floatingY.collectAsState()
    val presetSize by viewModel.floatingSizePreset.collectAsState()
    val customWidth by viewModel.floatingCustomWidth.collectAsState()

    val widthDp = customWidth.dp
    // calculate height in 16:9 aspect ratio
    val heightDp = widthDp * 9 / 16

    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .offset { IntOffset(floatingX.toInt(), floatingY.toInt()) }
            .width(widthDp)
            .height(heightDp + 36.dp) // extra 36dp for custom bottom controls bar!
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    viewModel.updateFloatingPosition(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Video component
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (video.isYouTubeVideo) {
                    YouTubeNativePlayer(
                        videoId = video.videoId,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    ExoVideoPlayer(
                        videoUrl = video.streamUrl!!,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Custom floating controls row inside the widget
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(Color(0xFF1E1C1C))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Resize controls preset indicators: S, M, L
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = { viewModel.setFloatingSizePreset("S") }
                    ) {
                        Text("S", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (presetSize == "S") Color.Red else Color.LightGray)
                    }

                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = { viewModel.setFloatingSizePreset("M") }
                    ) {
                        Text("M", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (presetSize == "M") Color.Red else Color.LightGray)
                    }

                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = { viewModel.setFloatingSizePreset("L") }
                    ) {
                        Text("L", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (presetSize == "L") Color.Red else Color.LightGray)
                    }
                }

                // Drag-to-Resize Handle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                viewModel.updateFloatingWidth(dragAmount.x / density.density)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AspectRatio,
                        contentDescription = "Drag to Resize",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Drag move handle indicator
                Icon(Icons.Filled.DragHandle, contentDescription = "Drag Move", modifier = Modifier.size(16.dp), tint = Color.Gray)

                // Minimize/restore & Dismiss actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = onRestore
                    ) {
                        Icon(Icons.Filled.Fullscreen, contentDescription = "Maximize", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    IconButton(
                        modifier = Modifier.size(24.dp),
                        onClick = { viewModel.setPlayingVideo(null) }
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}


// ---------------- FULL SCREEN PLAY PLAYER SHEETS ----------------
@Composable
fun VideoDetailsExpandedView(
    viewModel: TubeViewModel,
    video: VideoItem,
    onMinimizeToFloating: () -> Unit,
    onDismiss: () -> Unit,
    onAddToPlaylist: (VideoItem) -> Unit
) {
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }
    val playSpeed by viewModel.currentSpeed.collectAsState()
    
    // Check favorites database state dynamically
    val favoritesState by viewModel.favorites.collectAsState()
    LaunchedEffect(favoritesState, video) {
        isFavorite = favoritesState.any { it.videoId == video.videoId }
    }

    // High fidelity YouTube states
    val isSubscribedFlow = remember(video.channelTitle) { viewModel.isSubscribedFlow(video.channelTitle) }
    val isSubscribed by isSubscribedFlow.collectAsState(initial = false)
    var likesCount by remember(video.videoId) { mutableStateOf(video.title.length * 123 + 456) }
    var userLiked by remember(video.videoId) { mutableStateOf(false) }
    var userDisliked by remember(video.videoId) { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    val comments by viewModel.comments.collectAsState()
    var commentText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Player header close bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("Playing Video", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
            IconButton(onClick = onMinimizeToFloating) {
                Icon(Icons.Filled.PictureInPicture, contentDescription = "Float Window", tint = Color.White)
            }
        }

        // Active screen responsive video display window
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, SlateBorder, RoundedCornerShape(16.dp))
                .background(Color.Black)
        ) {
            if (video.isYouTubeVideo) {
                YouTubeNativePlayer(
                    videoId = video.videoId,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ExoVideoPlayer(
                    videoUrl = video.streamUrl!!,
                    modifier = Modifier.fillMaxSize(),
                    speed = playSpeed
                )
            }
        }

        // Metadata scroll body
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            item {
                Text(
                    video.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Stats row
            item {
                Text(
                    text = "${video.viewCount} views • ${video.publishedAt.takeIf { it.isNotEmpty() } ?: "Recently"}",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Channel Subscribe Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Channel Avatar with custom color
                        val avatarColor = remember(video.channelTitle) {
                            val colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF009688), Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFFFF5722))
                            colors[video.channelTitle.hashCode().coerceAtLeast(0) % colors.size]
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(avatarColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                video.channelTitle.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                video.channelTitle,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val subCount = remember(video.channelTitle) {
                                val base = (video.channelTitle.hashCode().coerceAtLeast(0) % 890 + 10) / 10.0
                                "${String.format("%.1f", base)}M subscribers"
                            }
                            Text(subCount, color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    // Subscribe button pill
                    Button(
                        onClick = {
                            viewModel.toggleSubscription(video.channelTitle)
                            Toast.makeText(context, if (!isSubscribed) "Subscribed to ${video.channelTitle}!" else "Unsubscribed from ${video.channelTitle}", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSubscribed) Color.DarkGray else Color.White,
                            contentColor = if (isSubscribed) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        if (isSubscribed) {
                            Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Subscribed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Subscribe", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Toolbar action row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Likes & Dislikes Segment
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF2B2B2B)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    if (userLiked) {
                                        userLiked = false
                                        likesCount--
                                    } else {
                                        userLiked = true
                                        likesCount++
                                        if (userDisliked) userDisliked = false
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ThumbUp,
                                contentDescription = "Like",
                                tint = if (userLiked) Color.Red else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (likesCount > 1000) String.format("%.1fK", likesCount / 1000.0) else likesCount.toString(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(16.dp)
                                .width(1.dp)
                                .background(Color.Gray.copy(alpha = 0.5f))
                        )

                        Row(
                            modifier = Modifier
                                .clickable {
                                    if (userDisliked) {
                                        userDisliked = false
                                    } else {
                                        userDisliked = true
                                        if (userLiked) {
                                            userLiked = false
                                            likesCount--
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ThumbDown,
                                contentDescription = "Dislike",
                                tint = if (userDisliked) Color.Red else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Share Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF2B2B2B))
                            .clickable {
                                val intent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Watch this: https://youtu.be/${video.videoId}")
                                    type = "text/plain"
                                }
                                context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Save Play Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isFavorite) MaterialTheme.colorScheme.primaryContainer else Color(0xFF2B2B2B))
                            .clickable {
                                viewModel.toggleFavorite(video)
                                Toast.makeText(context, if (isFavorite) "Removed from favorites" else "Saved to Favorites!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save", color = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Add to custom playlist Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF2B2B2B))
                            .clickable {
                                onAddToPlaylist(video)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.PlaylistAdd, contentDescription = "Add to Playlist", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Speed adjuster (Only for MP4 native play)
            if (!video.isYouTubeVideo) {
                item {
                    val speeds = listOf(0.5f, 1.0f, 1.5f, 2.0f)
                    Column {
                        Text("Playback Speed", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            speeds.forEach { sp ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (playSpeed == sp) Color.Red else MaterialTheme.colorScheme.surface)
                                        .clickable { viewModel.currentSpeed.value = sp }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${sp}x", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Description expanded Details Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDescriptionExpanded = !isDescriptionExpanded },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222020)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Description", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Icon(
                                imageVector = if (isDescriptionExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = video.description,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Comments Section Header
            item {
                Text(
                    text = "Comments (${comments.size})",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Add Comment Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFFF0000), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Y", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add a public comment...", color = Color.Gray, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp, max = 120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E1E),
                            unfocusedContainerColor = Color(0xFF1E1E1E),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        trailingIcon = {
                            if (commentText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        viewModel.addComment(video.videoId, "@You", commentText)
                                        commentText = ""
                                        Toast.makeText(context, "Comment posted!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post Comment", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    )
                }
            }

            // Comments List Items
            items(comments) { comment ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val commentAvatarColor = remember(comment.author) {
                            val colors = listOf(Color(0xFF3F51B5), Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF9C27B0), Color(0xFFE91E63), Color(0xFFFF9800), Color(0xFFFF5722))
                            colors[comment.author.hashCode().coerceAtLeast(0) % colors.size]
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(commentAvatarColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                comment.author.replace("@", "").take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    comment.author,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val elapsedText = remember(comment.timestamp) {
                                    val diff = System.currentTimeMillis() - comment.timestamp
                                    when {
                                        diff < 60000 -> "Just now"
                                        diff < 3600000 -> "${diff / 60000}m ago"
                                        diff < 86400000 -> "${diff / 3600000}h ago"
                                        else -> "${diff / 86400000}d ago"
                                    }
                                }
                                Text(
                                    elapsedText,
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                comment.text,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ThumbUp,
                                    contentDescription = "Like",
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clickable { viewModel.likeComment(comment) }
                                )
                                if (comment.likes > 0) {
                                    Text(
                                        comment.likes.toString(),
                                        color = Color.Gray,
                                        fontSize = 10.sp
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

// ---------------- VIDEO LIST ITEM CARD ----------------
@Composable
fun VideoListItemCard(
    video: VideoItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // Thumbnail with Duration Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = video.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Duration tag
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        video.duration,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // If it represents a Short, display a glowing vertical badge indicator
                if (video.isShort) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Red, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "SHORT",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Text Info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Channel profile icon representation
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.DarkGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        video.channelTitle.take(1).uppercase(Locale.ROOT),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Details Text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        video.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            video.channelTitle,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text("•", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            video.viewCount,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
