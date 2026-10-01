package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.WatchStatus
import com.example.ui.UiState
import com.example.ui.components.AnimeCover
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultBorderSubtle
import com.example.ui.theme.VaultCardElevated
import com.example.ui.theme.VaultCardSurface
import com.example.ui.theme.VioletAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    uiState: UiState,
    onUpdateProfile: (UserProfile) -> Unit,
    onStartNewSession: () -> Unit = {},
    onClearSessionLogs: () -> Unit = {},
    onResumeLastAnime: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditingName by remember { mutableStateOf(false) }
    var userNameInput by remember(uiState.userProfile.name) { mutableStateOf(uiState.userProfile.name) }

    val tasteProfile = uiState.tasteProfile
    val totalAnime = uiState.userLibrary.size
    val completedCount = uiState.userLibrary.count { it.userStatus == WatchStatus.COMPLETED }
    val watchingCount = uiState.userLibrary.count { it.userStatus == WatchStatus.WATCHING }
    val totalHours = tasteProfile.estimatedHoursWatched
    val totalDays = totalHours / 24.0
    val session = uiState.sessionInfo

    val timeFormatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // User Header Card
        item {
            Surface(
                color = VaultCardSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(VaultCardElevated)
                                .border(2.dp, CrimsonAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.userProfile.name.take(1).uppercase(),
                                color = CrimsonAccent,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            if (isEditingName) {
                                OutlinedTextField(
                                    value = userNameInput,
                                    onValueChange = { userNameInput = it },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CrimsonAccent,
                                        unfocusedBorderColor = VaultBorder,
                                        focusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Text(
                                        text = "Save",
                                        color = CrimsonAccent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable {
                                                onUpdateProfile(uiState.userProfile.copy(name = userNameInput.ifBlank { "Anime Fan" }))
                                                isEditingName = false
                                            }
                                            .padding(4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Cancel",
                                        color = TextSecondary,
                                        modifier = Modifier
                                            .clickable { isEditingName = false }
                                            .padding(4.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = uiState.userProfile.name,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "AnimeVault Explorer • ${uiState.userProfile.email}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Edit name",
                                    color = CrimsonAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { isEditingName = true }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Session Memory & Database Persistence (Room v2)
        item {
            SectionHeader(
                title = "Session Memory",
                subtitle = "Active state and SQLite Room session persistence"
            )

            Surface(
                color = VaultCardSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with live indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Session #${session.sessionNumber}",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Live status pill
                        Surface(
                            color = StatusCompleted.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusCompleted.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(StatusCompleted)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SQLite Active",
                                    color = StatusCompleted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3-Metric Session Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = VaultCardElevated,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Active Time", color = TextTertiary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = uiState.sessionDurationFormatted,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = VaultCardElevated,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Episodes", color = TextTertiary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${session.episodesWatchedThisSession}",
                                    color = CyanAccent,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = VaultCardElevated,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Saved Screen", color = TextTertiary, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = session.lastDestination,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Resume Last Viewed Anime Prompt (if any)
                    if (session.lastViewedAnimeTitle != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = CrimsonAccent.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = CrimsonAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "Last Viewed in Session", color = CrimsonAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = session.lastViewedAnimeTitle,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Button(
                                    onClick = onResumeLastAnime,
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Resume", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onStartNewSession,
                            border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "New Session", color = TextPrimary, fontSize = 12.sp)
                        }

                        if (uiState.sessionLogs.isNotEmpty()) {
                            OutlinedButton(
                                onClick = onClearSessionLogs,
                                border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Clear Logs", color = TextTertiary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section: Session History Log (Saved sessions from database)
        item {
            SectionHeader(
                title = "Session History Log",
                subtitle = "${uiState.sessionLogs.size} recorded sessions in database"
            )

            if (uiState.sessionLogs.isEmpty()) {
                Surface(
                    color = VaultCardSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Active session is tracking in SQLite Room.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tap 'New Session' above or restart the app to finalize and archive session entries.",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.sessionLogs.take(5).forEach { log ->
                        Surface(
                            color = VaultCardSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(VaultCardElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${log.sessionNumber}",
                                            color = CyanAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = timeFormatter.format(Date(log.startTime)),
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${log.durationMinutes} min • ${log.episodesWatchedCount} eps watched • ${log.lastDestination}",
                                            color = TextTertiary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Surface(
                                    color = VaultCardElevated,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${log.durationMinutes}m",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        // Section: Your Anime DNA (Personalized algorithm calculated from real user data)
        item {
            SectionHeader(
                title = "Your Anime DNA",
                subtitle = "Taste profile computed from your ratings & history"
            )

            if (!tasteProfile.hasSufficientData || tasteProfile.genrePercentages.isEmpty()) {
                Surface(
                    color = VaultCardSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Keep rating anime to build your taste profile.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Rate at least 2 anime or track 3 series to calculate your Anime DNA.",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                Surface(
                    color = VaultCardSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        tasteProfile.genrePercentages.entries.take(5).forEach { (genre, percentage) ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = genre,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "$percentage%",
                                        color = CrimsonAccent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { percentage / 100f },
                                    color = CrimsonAccent,
                                    trackColor = VaultCardElevated,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }
                        }

                        if (tasteProfile.topStudios.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "FAVORITE STUDIOS",
                                color = TextTertiary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tasteProfile.topStudios.joinToString(", "),
                                color = CyanAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Section: Personal Anime Statistics
        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionHeader(
                title = "Anime Statistics",
                subtitle = "Lifetime tracking and consumption dashboard"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        title = "Completed",
                        value = "$completedCount",
                        sub = "Anime finished",
                        icon = Icons.Default.CheckCircle,
                        accent = StatusCompleted,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Watching",
                        value = "$watchingCount",
                        sub = "Active series",
                        icon = Icons.Default.VideoLibrary,
                        accent = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        title = "Episodes",
                        value = "${tasteProfile.totalEpisodesWatched}",
                        sub = "Total watched",
                        icon = Icons.Default.Timer,
                        accent = VioletAccent,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        title = "Watch Time",
                        value = String.format(Locale.US, "%.1f h", totalHours),
                        sub = String.format(Locale.US, "%.1f days", totalDays),
                        icon = Icons.Default.HourglassBottom,
                        accent = CrimsonAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (tasteProfile.averageRating > 0.0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox(
                            title = "Average Rating",
                            value = "★ ${tasteProfile.averageRating}",
                            sub = "Across your ratings",
                            icon = Icons.Default.Star,
                            accent = GoldAccent,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "Total Library",
                            value = "$totalAnime",
                            sub = "In your vault",
                            icon = Icons.Default.BarChart,
                            accent = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section: Watch History Timeline
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "Recent Watch History",
                subtitle = "Timeline of episodes marked as watched"
            )

            if (uiState.watchHistory.isEmpty()) {
                Surface(
                    color = VaultCardSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "No episodes logged yet. Watch history builds automatically when you mark episodes as watched.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        items(uiState.watchHistory.take(8), key = { "history_${it.id}" }) { entry ->
            val dateStr = remember(entry.watchedAt) {
                SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(entry.watchedAt))
            }

            Surface(
                color = VaultCardSurface,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp)
                ) {
                    AnimeCover(
                        imageUrl = entry.animeImageUrl,
                        title = entry.animeTitle,
                        modifier = Modifier.size(width = 42.dp, height = 56.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.animeTitle,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Watched Episode ${entry.episodeNumber}",
                            color = CyanAccent,
                            fontSize = 12.sp
                        )
                        Text(
                            text = dateStr,
                            color = TextTertiary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Section: Release Notification Preferences
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "Release Preferences",
                subtitle = "Manage tracking awareness and alert options"
            )

            Surface(
                color = VaultCardSurface,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "New Episode Notifications", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Alerts when an episode drops today", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = uiState.userProfile.notifyNewEpisodes,
                            onCheckedChange = {
                                onUpdateProfile(uiState.userProfile.copy(notifyNewEpisodes = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonAccent)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "New Season Announcements", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Alert when followed anime gets a sequel", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = uiState.userProfile.notifyNewSeasons,
                            onCheckedChange = {
                                onUpdateProfile(uiState.userProfile.copy(notifyNewSeasons = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonAccent)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = VaultCardSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextSecondary, fontSize = 12.sp)
                Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = sub, color = TextTertiary, fontSize = 11.sp)
        }
    }
}
