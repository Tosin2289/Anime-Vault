package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AnimeItem
import com.example.data.model.WatchStatus
import com.example.data.remote.CatalogData
import com.example.ui.components.AnimeCover
import com.example.ui.components.EpisodeStepper
import com.example.ui.components.RatingStarsBar
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
fun AddAnimeDialog(
    initialAnime: AnimeItem?,
    onDismiss: () -> Unit,
    onSave: (AnimeItem, WatchStatus, Int, Int?, Boolean, String?) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedAnime by remember(initialAnime) { mutableStateOf(initialAnime) }
    var status by remember { mutableStateOf(WatchStatus.WATCHING) }
    var watchedEp by remember { mutableIntStateOf(0) }
    var rating by remember { mutableStateOf<Int?>(null) }
    var isFavorite by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) CatalogData.curatedCatalog
        else CatalogData.curatedCatalog.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    (it.englishTitle?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = VaultCardSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, VaultBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedAnime == null) "Select Anime" else "Add to Library",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                if (selectedAnime == null) {
                    // Search to choose anime
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search title to add...", color = TextTertiary, fontSize = 13.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CrimsonAccent) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonAccent,
                            unfocusedBorderColor = VaultBorder,
                            focusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        items(searchResults, key = { it.malId }) { anime ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VaultCardElevated)
                                    .clickable { selectedAnime = anime }
                                    .padding(8.dp)
                            ) {
                                AnimeCover(imageUrl = anime.imageUrl, title = anime.title, modifier = Modifier.size(width = 44.dp, height = 60.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = anime.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text(text = anime.genres.take(2).joinToString(", "), color = TextTertiary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else {
                    val anime = selectedAnime!!
                    // Selected Anime Summary Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(VaultCardElevated)
                            .padding(10.dp)
                    ) {
                        AnimeCover(imageUrl = anime.imageUrl, title = anime.title, modifier = Modifier.size(width = 50.dp, height = 70.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = anime.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(text = anime.genres.take(3).joinToString(" • "), color = TextTertiary, fontSize = 11.sp)
                            Text(
                                text = "Change anime",
                                color = CrimsonAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { selectedAnime = null }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Watch Status Selection
                    Text(text = "Watch Status", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        items(WatchStatus.entries) { st ->
                            val isSelected = status == st
                            FilterChip(
                                selected = isSelected,
                                onClick = { status = st },
                                label = { Text(st.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CrimsonAccent,
                                    selectedLabelColor = Color.White,
                                    containerColor = VaultCardElevated,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Episodes Watched", color = TextSecondary, fontSize = 12.sp)
                        EpisodeStepper(
                            current = watchedEp,
                            total = anime.episodesTotal,
                            onIncrement = { watchedEp++ },
                            onDecrement = { if (watchedEp > 0) watchedEp-- }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rating
                    Text(text = "Rating" + if (rating != null) " ($rating/10)" else "", color = TextSecondary, fontSize = 12.sp)
                    RatingStarsBar(rating = rating, onRatingSelected = { r -> rating = if (r == 0) null else r })

                    Spacer(modifier = Modifier.height(10.dp))

                    // Favorite Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFavorite = !isFavorite }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFavorite) CrimsonAccent else TextTertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Mark as Favorite", color = TextPrimary, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Add Button
                    Button(
                        onClick = {
                            onSave(anime, status, watchedEp, rating, isFavorite, notes.ifBlank { null })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_anime_confirm_button")
                    ) {
                        Text(text = "Save to My Anime", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
