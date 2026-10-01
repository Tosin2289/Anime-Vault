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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.model.AnimeNewsItem
import com.example.data.model.Recommendation
import com.example.data.model.WatchStatus
import com.example.ui.UiState
import com.example.ui.components.AnimeCover
import com.example.ui.components.AnimeNewsCard
import com.example.ui.components.AnimeReleaseCard
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.EpisodeStepper
import com.example.ui.components.ProgressBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StatusWatching
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultBorderSubtle
import com.example.ui.theme.VaultCardElevated
import com.example.ui.theme.VaultCardSurface
import java.util.Calendar

@Composable
fun HomeScreen(
    uiState: UiState,
    onAnimeClick: (AnimeItem) -> Unit,
    onIncrementProgress: (AnimeItem) -> Unit,
    onDecrementProgress: (AnimeItem) -> Unit,
    onMarkWatched: (AnimeItem) -> Unit,
    onAddToLibrary: (AnimeItem) -> Unit,
    onComingSoonFilterChange: (String) -> Unit,
    onNavigateToDiscover: () -> Unit,
    onNewsClick: (AnimeNewsItem) -> Unit = {},
    onSurpriseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Personalized Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting, ${uiState.userProfile.name}",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Realtime Jikan v4 Active",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(
                    color = VaultCardElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle),
                    modifier = Modifier.clickable { onSurpriseClick() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Random Anime",
                            tint = CrimsonAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Random",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 2. Quick Statistics Bar
        item {
            QuickStatsRow(uiState = uiState)
        }

        // 3. Continue Watching Section
        item {
            SectionHeader(
                title = "Continue Watching",
                subtitle = "${uiState.continueWatching.size} active anime"
            )
            if (uiState.continueWatching.isEmpty()) {
                EmptyStateCard(
                    title = "No anime currently in progress",
                    description = "Add anime from search or discovery to start tracking your watch journey.",
                    actionLabel = "Discover Anime",
                    onActionClick = onNavigateToDiscover
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.continueWatching, key = { it.malId }) { anime ->
                        ContinueWatchingCard(
                            anime = anime,
                            onClick = { onAnimeClick(anime) },
                            onIncrement = { onIncrementProgress(anime) },
                            onDecrement = { onDecrementProgress(anime) },
                            onMarkWatched = { onMarkWatched(anime) }
                        )
                    }
                }
            }
        }

        // 4. New Episodes Today Section
        if (uiState.newEpisodesToday.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionHeader(
                    title = "New Episodes Today",
                    subtitle = "Fresh releases from your tracked anime"
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.newEpisodesToday.forEach { anime ->
                        NewEpisodeTodayCard(
                            anime = anime,
                            onViewAnime = { onAnimeClick(anime) },
                            onMarkWatched = { onMarkWatched(anime) }
                        )
                    }
                }
            }
        }

        // 5. Coming Soon with Today/Tomorrow/This Week filter
        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionHeader(
                title = "Coming Soon",
                subtitle = "Upcoming episodes from anime you follow"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Today", "Tomorrow", "This Week").forEach { filter ->
                    val isSelected = uiState.comingSoonFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onComingSoonFilterChange(filter) },
                        label = { Text(filter, fontSize = 12.sp) },
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

            if (uiState.comingSoon.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VaultCardSurface)
                        .border(1.dp, VaultBorderSubtle, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No airing episodes scheduled for ${uiState.comingSoonFilter.lowercase()}.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.comingSoon.take(6), key = { "coming_${it.malId}" }) { anime ->
                        ComingSoonCard(anime = anime, onClick = { onAnimeClick(anime) })
                    }
                }
            }
        }

        // 6. Recommended For You (Deterministic Recommendation Engine)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SectionHeader(
                title = "Recommended For You",
                subtitle = "Personalized from your watch patterns & ratings",
                actionLabel = "Explore All",
                onActionClick = onNavigateToDiscover
            )
            if (uiState.recommendations.isEmpty()) {
                EmptyStateCard(
                    title = "Learning your taste",
                    description = "Rate a few anime and track your episodes to unlock hyper-personalized recommendations.",
                    actionLabel = "Add Anime",
                    onActionClick = onNavigateToDiscover
                )
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.recommendations.take(4).forEach { rec ->
                        RecommendationCard(
                            recommendation = rec,
                            onClick = { onAnimeClick(rec.anime) },
                            onAdd = { onAddToLibrary(rec.anime) }
                        )
                    }
                }
            }
        }

        // 7. Upcoming Episode Airing Releases (AniList Live Schedule)
        if (uiState.upcomingReleases.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Upcoming Airing Releases",
                    subtitle = "Live episode schedule & countdowns"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.upcomingReleases.take(10)) { release ->
                        AnimeReleaseCard(
                            release = release,
                            onClick = {
                                val dummyAnime = AnimeItem(
                                    malId = release.malId,
                                    title = release.title,
                                    imageUrl = release.imageUrl,
                                    synopsis = "Episode ${release.episodeNumber} broadcasting soon.",
                                    genres = release.genres,
                                    isAiring = true,
                                    airingStatus = "Currently Airing"
                                )
                                onAnimeClick(dummyAnime)
                            }
                        )
                    }
                }
            }
        }

        // 8. Anime News & Industry Releases (Live MAL RSS / Breaking News)
        if (uiState.animeNews.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader(
                    title = "Breaking Anime News",
                    subtitle = "New season announcements, trailers & industry updates"
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.animeNews.take(5).forEach { news ->
                        AnimeNewsCard(
                            newsItem = news,
                            onClick = { onNewsClick(news) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QuickStatsRow(uiState: UiState) {
    val completed = uiState.userLibrary.count { it.userStatus == WatchStatus.COMPLETED }
    val watching = uiState.userLibrary.count { it.userStatus == WatchStatus.WATCHING }
    val planToWatch = uiState.userLibrary.count { it.userStatus == WatchStatus.PLAN_TO_WATCH }
    val episodes = uiState.tasteProfile.totalEpisodesWatched
    val hours = (episodes * 24) / 60
    val avgRating = uiState.tasteProfile.averageRating

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            StatPill(label = "Watching", value = "$watching", accent = CyanAccent)
        }
        item {
            StatPill(label = "Completed", value = "$completed", accent = CrimsonAccent)
        }
        item {
            StatPill(label = "Plan to Watch", value = "$planToWatch", accent = GoldAccent)
        }
        item {
            StatPill(label = "Episodes", value = "$episodes", accent = TextPrimary)
        }
        item {
            StatPill(label = "Watch Time", value = "${hours}h", accent = TextPrimary)
        }
        if (avgRating > 0.0) {
            item {
                StatPill(label = "Avg Rating", value = "★ $avgRating", accent = GoldAccent)
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String, accent: Color) {
    Surface(
        color = VaultCardSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle),
        modifier = Modifier.height(64.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun ContinueWatchingCard(
    anime: AnimeItem,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onMarkWatched: () -> Unit
) {
    val total = anime.episodesTotal
    val watched = anime.episodesWatched
    val progress = if (total != null && total > 0) watched.toFloat() / total.toFloat() else 0.5f
    val pct = (progress * 100).toInt().coerceIn(0, 100)

    Surface(
        color = VaultCardSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
        modifier = Modifier
            .width(280.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimeCover(
                    imageUrl = anime.imageUrl,
                    title = anime.title,
                    modifier = Modifier.size(width = 68.dp, height = 96.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = anime.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ep $watched" + if (total != null) " / $total" else "",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (anime.broadcastDay != null) {
                        Text(
                            text = "Next: ${anime.broadcastDay}",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "$pct% complete",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
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

                Surface(
                    color = CrimsonAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { onMarkWatched() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark watched",
                            tint = CrimsonAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+1 Watched",
                            color = CrimsonAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewEpisodeTodayCard(
    anime: AnimeItem,
    onViewAnime: () -> Unit,
    onMarkWatched: () -> Unit
) {
    val nextEp = anime.episodesWatched + 1
    val releaseTime = anime.broadcastTime ?: "6:30 PM"

    Surface(
        color = VaultCardElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            AnimeCover(
                imageUrl = anime.imageUrl,
                title = anime.title,
                modifier = Modifier.size(width = 54.dp, height = 72.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CrimsonAccent,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "NEW TODAY",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Releases at $releaseTime",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
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
                    text = "Episode $nextEp is airing today",
                    color = CyanAccent,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = CrimsonAccent,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onMarkWatched() }
                ) {
                    Text(
                        text = "Watched",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Details",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { onViewAnime() }
                )
            }
        }
    }
}

@Composable
fun ComingSoonCard(anime: AnimeItem, onClick: () -> Unit) {
    Surface(
        color = VaultCardSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle),
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AnimeCover(
                imageUrl = anime.imageUrl,
                title = anime.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = anime.title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = anime.broadcastDay ?: "Airing weekly",
                color = CyanAccent,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun RecommendationCard(
    recommendation: Recommendation,
    onClick: () -> Unit,
    onAdd: () -> Unit
) {
    val anime = recommendation.anime

    Surface(
        color = VaultCardSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                AnimeCover(
                    imageUrl = anime.imageUrl,
                    title = anime.title,
                    modifier = Modifier.size(width = 68.dp, height = 96.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = CrimsonAccent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${recommendation.matchPercentage}% Match",
                                color = CrimsonAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (anime.score != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${anime.score}",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
                        text = anime.genres.take(3).joinToString(" • "),
                        color = TextTertiary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = VaultCardElevated,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = recommendation.explanation,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = VaultCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
                    modifier = Modifier.clickable { onAdd() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to library",
                            tint = CrimsonAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add to Library",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
