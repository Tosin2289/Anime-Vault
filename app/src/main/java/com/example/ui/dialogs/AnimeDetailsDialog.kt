package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.AnimeItem
import com.example.data.model.EpisodeItem
import com.example.data.model.WatchStatus
import com.example.ui.components.AnimeCover
import com.example.ui.components.EpisodeStepper
import com.example.ui.components.RatingStarsBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VaultBlack
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultBorderSubtle
import com.example.ui.theme.VaultCardElevated
import com.example.ui.theme.VaultCardSurface

@Composable
fun AnimeDetailsDialog(
    anime: AnimeItem,
    episodes: List<EpisodeItem>,
    watchedEpisodeNumbers: Set<Int>,
    onDismiss: () -> Unit,
    onStatusChange: (WatchStatus) -> Unit,
    onProgressChange: (Int) -> Unit,
    onRatingChange: (Int?) -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onNotesChange: (String?) -> Unit,
    onDeleteFromLibrary: () -> Unit,
    onToggleEpisode: (EpisodeItem) -> Unit,
    onRelatedAnimeClick: (Int, String) -> Unit
) {
    val context = LocalContext.current
    var selectedStatus by remember(anime.userStatus) { mutableStateOf(anime.userStatus ?: WatchStatus.WATCHING) }
    var notesText by remember(anime.userNotes) { mutableStateOf(anime.userNotes ?: "") }
    var showNotesInput by remember { mutableStateOf(anime.userNotes != null) }

    val hasSequel = remember(anime.relations) {
        anime.relations.any { it.relationType.contains("Sequel", ignoreCase = true) }
    }
    val sequelRelation = remember(anime.relations) {
        anime.relations.firstOrNull { it.relationType.contains("Sequel", ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = VaultBlack,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Image with Close & Favorite Buttons
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        if (anime.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = anime.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(VaultCardElevated)
                            )
                        }

                        // Gradient Scrim
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            VaultBlack.copy(alpha = 0.7f),
                                            VaultBlack
                                        )
                                    )
                                )
                        )

                        // Action Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(VaultBlack.copy(alpha = 0.6f))
                                    .testTag("details_close_button")
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                            }

                            Row {
                                if (anime.userStatus != null) {
                                    IconButton(
                                        onClick = { onFavoriteToggle(anime.isFavorite) },
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(VaultBlack.copy(alpha = 0.6f))
                                            .testTag("details_favorite_toggle")
                                    ) {
                                        Icon(
                                            imageVector = if (anime.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favorite",
                                            tint = if (anime.isFavorite) CrimsonAccent else TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Anime Main Title and Meta Info
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (anime.score != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "${anime.score} MAL", color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    if (anime.rank != null) {
                                        Text(text = " • #${anime.rank}", color = TextSecondary, fontSize = 13.sp)
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = anime.airingStatus,
                                    color = if (anime.isAiring) CyanAccent else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Surface(
                                    color = VaultCardElevated,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle),
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://myanimelist.net/anime/${anime.malId}"))
                                        context.startActivity(intent)
                                    }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(text = "MAL #${anime.malId}", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open MAL", tint = CyanAccent, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = anime.englishTitle ?: anime.title,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        )

                        if (!anime.japaneseTitle.isNullOrBlank()) {
                            Text(
                                text = anime.japaneseTitle,
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tags (Genres, Studios, Season)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(anime.genres) { genre ->
                                Surface(
                                    color = VaultCardElevated,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle)
                                ) {
                                    Text(text = genre, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                            items(anime.studios) { studio ->
                                Surface(
                                    color = CyanAccent.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
                                ) {
                                    Text(text = studio, color = CyanAccent, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }

                // Season / Sequel Awareness Banner
                if (hasSequel && sequelRelation != null) {
                    item {
                        Surface(
                            color = CrimsonAccent.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NewReleases,
                                    contentDescription = "New Season Alert",
                                    tint = CrimsonAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "NEW SEASON / SEQUEL",
                                        color = CrimsonAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = sequelRelation.title,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    color = CrimsonAccent,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable {
                                        onRelatedAnimeClick(sequelRelation.malId, sequelRelation.title)
                                    }
                                ) {
                                    Text(
                                        text = "View",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Tracking / Library Status Controls
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
                            Text(
                                text = "YOUR LIBRARY STATUS",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Watch Status Chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(WatchStatus.entries) { status ->
                                    val isSelected = (anime.userStatus ?: selectedStatus) == status
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedStatus = status
                                            onStatusChange(status)
                                        },
                                        label = { Text(status.displayName, fontSize = 12.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CrimsonAccent,
                                            selectedLabelColor = Color.White,
                                            containerColor = VaultCardElevated,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = if (isSelected) CrimsonAccent else VaultBorderSubtle,
                                            enabled = true,
                                            selected = isSelected
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Episode Progress Stepper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Episode Progress", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = if (anime.episodesTotal != null) "${anime.episodesTotal} total episodes" else "Ongoing series",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                EpisodeStepper(
                                    current = anime.episodesWatched,
                                    total = anime.episodesTotal,
                                    onIncrement = { onProgressChange(anime.episodesWatched + 1) },
                                    onDecrement = { if (anime.episodesWatched > 0) onProgressChange(anime.episodesWatched - 1) }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Rating Bar
                            Text(
                                text = "Your Rating" + if (anime.userRating != null) " (${anime.userRating}/10)" else "",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            RatingStarsBar(
                                rating = anime.userRating,
                                onRatingSelected = { r -> onRatingChange(if (r == 0) null else r) }
                            )

                            // Notes Section
                            Spacer(modifier = Modifier.height(14.dp))
                            if (showNotesInput) {
                                OutlinedTextField(
                                    value = notesText,
                                    onValueChange = {
                                        notesText = it
                                        onNotesChange(it.ifBlank { null })
                                    },
                                    placeholder = { Text("Add personal notes, thoughts, or review...", color = TextTertiary, fontSize = 13.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CrimsonAccent,
                                        unfocusedBorderColor = VaultBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "+ Add personal notes / thoughts",
                                    color = CyanAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable { showNotesInput = true }
                                )
                            }

                            if (anime.userStatus != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onDeleteFromLibrary() },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = CrimsonAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Remove from Library", color = CrimsonAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Synopsis
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = "SYNOPSIS",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = anime.synopsis,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Episodes Section
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "EPISODES (${episodes.size})",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                items(episodes, key = { "ep_${it.episodeNumber}" }) { episode ->
                    val isWatched = episode.episodeNumber in watchedEpisodeNumbers || episode.episodeNumber <= anime.episodesWatched

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
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "EP ${episode.episodeNumber}",
                                color = if (isWatched) CyanAccent else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(54.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = episode.title,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (episode.isFiller) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = CrimsonAccent.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "FILLER",
                                                color = CrimsonAccent,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                episode.airedDate?.let {
                                    Text(text = it, color = TextTertiary, fontSize = 11.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { onToggleEpisode(episode) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isWatched) CrimsonAccent else VaultCardElevated)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Toggle watched",
                                    tint = if (isWatched) Color.White else TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}
