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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.data.model.AnimeNewsItem
import com.example.data.model.WatchStatus
import com.example.ui.UiState
import com.example.ui.components.AnimeCover
import com.example.ui.components.AnimeNewsCard
import com.example.ui.components.AnimeReleaseCard
import com.example.ui.components.SectionHeader
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
fun DiscoverScreen(
    uiState: UiState,
    onAnimeClick: (AnimeItem) -> Unit,
    onSearchChange: (String) -> Unit,
    onAddToLibrary: (AnimeItem) -> Unit,
    onNewsClick: (AnimeNewsItem) -> Unit = {},
    onSurpriseClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSectionTab by remember { mutableStateOf("Trending") }
    val sectionTabs = listOf("Trending", "Currently Airing", "Upcoming", "News & Releases", "Hidden Gems")

    val userFavorite = remember(uiState.userLibrary) {
        uiState.userLibrary.firstOrNull { it.isFavorite || (it.userRating ?: 0) >= 8 }
            ?: uiState.userLibrary.firstOrNull()
    }

    val becauseYouLikedList = remember(userFavorite, uiState.trendingAnime) {
        if (userFavorite == null) emptyList()
        else {
            val userFavGenres = userFavorite.genres.toSet()
            uiState.trendingAnime.filter { it.malId != userFavorite.malId && it.genres.any { g -> g in userFavGenres } }
        }
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Discover Anime",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
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
                            text = "Jikan v4 Realtime Active",
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
                            contentDescription = "Surprise Me",
                            tint = CrimsonAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Surprise Me",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Global Search Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search titles, genres, or studios...", color = TextTertiary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CrimsonAccent
                    )
                },
                trailingIcon = {
                    if (uiState.isSearching) {
                        CircularProgressIndicator(
                            color = CrimsonAccent,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CrimsonAccent,
                    unfocusedBorderColor = VaultBorder,
                    focusedContainerColor = VaultCardSurface,
                    unfocusedContainerColor = VaultCardSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("discover_search_input")
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Powered by Jikan REST API • Realtime MAL Index",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
        }

        // If actively searching, show search results
        if (uiState.searchQuery.isNotBlank()) {
            item {
                Text(
                    text = "SEARCH RESULTS (${uiState.searchResults.size})",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            if (uiState.searchResults.isEmpty() && !uiState.isSearching) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No anime found for '${uiState.searchQuery}'. Try another query.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(uiState.searchResults, key = { "search_${it.malId}" }) { anime ->
                    DiscoverItemCard(
                        anime = anime,
                        onClick = { onAnimeClick(anime) },
                        onAdd = { onAddToLibrary(anime) }
                    )
                }
            }
        } else {
            // "Because You Liked [Anime]" Section
            if (userFavorite != null && becauseYouLikedList.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    SectionHeader(
                        title = "Because You Liked ${userFavorite.title}",
                        subtitle = "Shared genres & storytelling themes"
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(becauseYouLikedList.take(6), key = { "because_${it.malId}" }) { anime ->
                            DiscoverHorizontalCard(
                                anime = anime,
                                onClick = { onAnimeClick(anime) },
                                onAdd = { onAddToLibrary(anime) }
                            )
                        }
                    }
                }
            }

            // Section Filter Tabs (Trending, Airing, Upcoming, Hidden Gems)
            item {
                Spacer(modifier = Modifier.height(18.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sectionTabs) { tab ->
                        val isSelected = selectedSectionTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSectionTab = tab },
                            label = { Text(tab, fontSize = 12.sp) },
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

            // Feed according to selected tab
            if (selectedSectionTab == "News & Releases") {
                if (uiState.upcomingReleases.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "AIRING EPISODE COUNTDOWNS",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.upcomingReleases) { rel ->
                                AnimeReleaseCard(
                                    release = rel,
                                    onClick = {
                                        val dummyAnime = AnimeItem(
                                            malId = rel.malId,
                                            title = rel.title,
                                            imageUrl = rel.imageUrl,
                                            synopsis = "Episode ${rel.episodeNumber} countdown.",
                                            genres = rel.genres,
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

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "LATEST INDUSTRY HEADLINES (${uiState.animeNews.size})",
                        color = CrimsonAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                items(uiState.animeNews, key = { "news_${it.id}" }) { news ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        AnimeNewsCard(
                            newsItem = news,
                            onClick = { onNewsClick(news) }
                        )
                    }
                }
            } else {
                val displayList = when (selectedSectionTab) {
                    "Trending" -> uiState.trendingAnime
                    "Currently Airing" -> uiState.airingAnime
                    "Upcoming" -> uiState.upcomingAnime
                    "Hidden Gems" -> uiState.trendingAnime.filter { (it.score ?: 0.0) >= 8.5 && (it.popularity ?: 0) > 40 }
                    else -> uiState.trendingAnime
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$selectedSectionTab (${displayList.size})".uppercase(),
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                items(displayList, key = { "feed_${it.malId}" }) { anime ->
                    DiscoverItemCard(
                        anime = anime,
                        onClick = { onAnimeClick(anime) },
                        onAdd = { onAddToLibrary(anime) }
                    )
                }
            }
        }
    }
}

@Composable
fun DiscoverHorizontalCard(
    anime: AnimeItem,
    onClick: () -> Unit,
    onAdd: () -> Unit
) {
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
                    .height(160.dp)
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
                text = anime.genres.firstOrNull() ?: "Anime",
                color = TextTertiary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = VaultCardElevated,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAdd() }
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = CrimsonAccent, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun DiscoverItemCard(
    anime: AnimeItem,
    onClick: () -> Unit,
    onAdd: () -> Unit
) {
    Surface(
        color = VaultCardSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            AnimeCover(
                imageUrl = anime.imageUrl,
                title = anime.title,
                modifier = Modifier.size(width = 68.dp, height = 94.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    anime.season?.let {
                        Text(
                            text = "${it.replaceFirstChar { c -> c.uppercase() }} ${anime.year ?: ""}",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = anime.synopsis,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onAdd,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(VaultCardElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to library",
                    tint = CrimsonAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
