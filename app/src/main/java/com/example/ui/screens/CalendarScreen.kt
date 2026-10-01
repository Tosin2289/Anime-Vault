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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnimeItem
import com.example.ui.UiState
import com.example.ui.components.AnimeCover
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultBorderSubtle
import com.example.ui.theme.VaultCardElevated
import com.example.ui.theme.VaultCardSurface
import java.util.Calendar
import java.util.TimeZone

@Composable
fun CalendarScreen(
    uiState: UiState,
    onAnimeClick: (AnimeItem) -> Unit,
    onDaySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("Today", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val localTz = remember { TimeZone.getDefault().id }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Release Calendar",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Airing schedule & upcoming episode radar",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Timezone indicator
                    Surface(
                        color = VaultCardElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorderSubtle)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Timezone",
                                tint = CyanAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = localTz.take(12),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Days Carousel
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                items(days) { day ->
                    val isSelected = uiState.selectedCalendarDay.equals(day, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onDaySelected(day) },
                        label = {
                            Text(
                                text = day,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
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

        // Schedule Releases
        if (uiState.calendarSchedule.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No anime scheduled for ${uiState.selectedCalendarDay}",
                    description = "Try selecting a different day or check your followed airing anime in My Anime.",
                    icon = Icons.Default.CalendarMonth,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        } else {
            item {
                Text(
                    text = "RELEASES (${uiState.calendarSchedule.size})",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(uiState.calendarSchedule, key = { "cal_${it.malId}" }) { anime ->
                CalendarReleaseCard(
                    anime = anime,
                    onClick = { onAnimeClick(anime) }
                )
            }
        }
    }
}

@Composable
fun CalendarReleaseCard(
    anime: AnimeItem,
    onClick: () -> Unit
) {
    val broadcastTime = anime.broadcastTime ?: "18:30"
    val nextEpisodeNum = anime.episodesWatched + 1

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
            // Airing Time Column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(58.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(VaultCardElevated)
                    .padding(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = broadcastTime,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Anime Cover
            AnimeCover(
                imageUrl = anime.imageUrl,
                title = anime.title,
                modifier = Modifier.size(width = 54.dp, height = 72.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Anime Information
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anime.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Episode $nextEpisodeNum",
                    color = CyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = anime.genres.take(2).joinToString(" • "),
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = CrimsonAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAccent.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "View",
                    color = CrimsonAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
