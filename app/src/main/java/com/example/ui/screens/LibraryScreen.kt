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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnimeItem
import com.example.data.model.WatchStatus
import com.example.ui.LibrarySort
import com.example.ui.UiState
import com.example.ui.components.AnimeCover
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EpisodeStepper
import com.example.ui.components.ProgressBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultBorderSubtle
import com.example.ui.theme.VaultCardElevated
import com.example.ui.theme.VaultCardSurface

@Composable
fun LibraryScreen(
    uiState: UiState,
    onAnimeClick: (AnimeItem) -> Unit,
    onIncrementProgress: (AnimeItem) -> Unit,
    onDecrementProgress: (AnimeItem) -> Unit,
    onStatusTabSelected: (String) -> Unit,
    onSortSelected: (LibrarySort) -> Unit,
    onGenreFilterSelected: (String?) -> Unit,
    onOpenAddAnime: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    val statusTabs = listOf(
        "ALL" to "All",
        "WATCHING" to "Watching",
        "COMPLETED" to "Completed",
        "PLAN_TO_WATCH" to "Plan to Watch",
        "ON_HOLD" to "On Hold",
        "DROPPED" to "Dropped",
        "FAVORITES" to "Favorites"
    )

    val allGenres = remember(uiState.userLibrary) {
        uiState.userLibrary.flatMap { it.genres }.distinct().sorted()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Anime Library",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.filteredLibrary.size} anime displayed",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                // Sort Action Button
                Box {
                    Surface(
                        color = VaultCardElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
                        modifier = Modifier.clickable { showSortMenu = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = CrimsonAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = uiState.selectedSort.displayName,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(VaultCardElevated)
                    ) {
                        LibrarySort.entries.forEach { sort ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = sort.displayName,
                                        color = if (uiState.selectedSort == sort) CrimsonAccent else TextPrimary,
                                        fontWeight = if (uiState.selectedSort == sort) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSortSelected(sort)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Status Tabs Row
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(statusTabs) { (tabKey, label) ->
                    val isSelected = uiState.selectedStatusTab == tabKey
                    val count = when (tabKey) {
                        "ALL" -> uiState.userLibrary.size
                        "WATCHING" -> uiState.userLibrary.count { it.userStatus == WatchStatus.WATCHING }
                        "COMPLETED" -> uiState.userLibrary.count { it.userStatus == WatchStatus.COMPLETED }
                        "PLAN_TO_WATCH" -> uiState.userLibrary.count { it.userStatus == WatchStatus.PLAN_TO_WATCH }
                        "ON_HOLD" -> uiState.userLibrary.count { it.userStatus == WatchStatus.ON_HOLD }
                        "DROPPED" -> uiState.userLibrary.count { it.userStatus == WatchStatus.DROPPED }
                        "FAVORITES" -> uiState.userLibrary.count { it.isFavorite }
                        else -> 0
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusTabSelected(tabKey) },
                        label = { Text("$label ($count)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonAccent,
                            selectedLabelColor = Color.White,
                            containerColor = VaultCardSurface,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) CrimsonAccent else VaultBorder,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

        // Genre Filter Chips Row (if any genres exist)
        if (allGenres.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedGenreFilter == null,
                            onClick = { onGenreFilterSelected(null) },
                            label = { Text("All Genres", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                selectedLabelColor = CyanAccent,
                                containerColor = VaultCardSurface,
                                labelColor = TextTertiary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (uiState.selectedGenreFilter == null) CyanAccent else VaultBorderSubtle,
                                enabled = true,
                                selected = uiState.selectedGenreFilter == null
                            )
                        )
                    }
                    items(allGenres) { genre ->
                        val isSelected = uiState.selectedGenreFilter == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = { onGenreFilterSelected(if (isSelected) null else genre) },
                            label = { Text(genre, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.2f),
                                selectedLabelColor = CyanAccent,
                                containerColor = VaultCardSurface,
                                labelColor = TextTertiary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) CyanAccent else VaultBorderSubtle,
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }
        }

        // Anime List
        if (uiState.filteredLibrary.isEmpty()) {
            item {
                EmptyStateCard(
                    title = if (uiState.userLibrary.isEmpty()) "Your anime journey starts here" else "No anime in this filter",
                    description = if (uiState.userLibrary.isEmpty())
                        "Add your first anime to your vault and we will start tracking your watch journey and learning your taste."
                    else "Try changing the status tab or clearing your genre filter.",
                    actionLabel = if (uiState.userLibrary.isEmpty()) "+ Add Anime" else null,
                    onActionClick = if (uiState.userLibrary.isEmpty()) onOpenAddAnime else null,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        } else {
            items(uiState.filteredLibrary, key = { it.malId }) { anime ->
                LibraryItemCard(
                    anime = anime,
                    onClick = { onAnimeClick(anime) },
                    onIncrement = { onIncrementProgress(anime) },
                    onDecrement = { onDecrementProgress(anime) }
                )
            }
        }
    }
}

@Composable
fun LibraryItemCard(
    anime: AnimeItem,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val total = anime.episodesTotal
    val watched = anime.episodesWatched
    val progress = if (total != null && total > 0) watched.toFloat() / total.toFloat() else 0f
    val pct = (progress * 100).toInt().coerceIn(0, 100)

    Surface(
        color = VaultCardSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimeCover(
                    imageUrl = anime.imageUrl,
                    title = anime.title,
                    modifier = Modifier.size(width = 64.dp, height = 90.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        anime.userStatus?.let { StatusBadge(status = it) }
                        if (anime.isFavorite) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorite",
                                tint = CrimsonAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = anime.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = anime.genres.take(2).joinToString(" • "),
                        color = TextTertiary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ep $watched" + if (total != null) " / $total" else "" + " ($pct%)",
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (anime.userRating != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${anime.userRating}/10",
                                    color = GoldAccent,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            ProgressBar(progress = progress)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EpisodeStepper(
                    current = watched,
                    total = total,
                    onIncrement = onIncrement,
                    onDecrement = onDecrement
                )

                Text(
                    text = "Tap for full details →",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
