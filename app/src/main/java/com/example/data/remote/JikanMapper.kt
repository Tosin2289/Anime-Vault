package com.example.data.remote

import com.example.data.model.AnimeItem
import com.example.data.model.AnimeRelation

object JikanMapper {
    fun toDomain(dto: JikanAnimeData): AnimeItem {
        val imageUrl = dto.images?.webp?.largeImageUrl
            ?: dto.images?.jpg?.largeImageUrl
            ?: dto.images?.webp?.imageUrl
            ?: dto.images?.jpg?.imageUrl
            ?: ""

        val genres = dto.genres?.mapNotNull { it.name } ?: emptyList()
        val themes = dto.themes?.mapNotNull { it.name } ?: emptyList()
        val studios = dto.studios?.mapNotNull { it.name } ?: emptyList()

        val relations = mutableListOf<AnimeRelation>()
        dto.relations?.forEach { rel ->
            val relType = rel.relation ?: "Related"
            rel.entry?.forEach { item ->
                if (item.malId != null && !item.name.isNullOrBlank()) {
                    relations.add(
                        AnimeRelation(
                            relationType = relType,
                            malId = item.malId,
                            title = item.name,
                            type = item.type ?: "Anime"
                        )
                    )
                }
            }
        }

        return AnimeItem(
            malId = dto.malId,
            title = dto.title,
            englishTitle = dto.titleEnglish,
            japaneseTitle = dto.titleJapanese,
            imageUrl = imageUrl,
            bannerUrl = imageUrl, // Jikan doesn't provide distinct banners on all endpoints, large cover acts as backdrop
            synopsis = dto.synopsis ?: "No synopsis available.",
            episodesTotal = dto.episodes,
            episodesWatched = 0,
            score = dto.score,
            rank = dto.rank,
            popularity = dto.popularity,
            airingStatus = dto.status ?: if (dto.airing == true) "Currently Airing" else "Finished Airing",
            isAiring = dto.airing == true,
            airedString = dto.aired?.string,
            broadcastDay = dto.broadcast?.day,
            broadcastTime = dto.broadcast?.time,
            broadcastTimezone = dto.broadcast?.timezone ?: "Asia/Tokyo",
            season = dto.season,
            year = dto.year,
            durationMinutes = 24,
            genres = genres,
            themes = themes,
            studios = studios,
            relations = relations
        )
    }
}
