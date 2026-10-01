package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JikanListResponse(
    val data: List<JikanAnimeData>? = null
)

@JsonClass(generateAdapter = true)
data class JikanSingleResponse(
    val data: JikanAnimeData? = null
)

@JsonClass(generateAdapter = true)
data class JikanEpisodesResponse(
    val data: List<JikanEpisodeData>? = null
)

@JsonClass(generateAdapter = true)
data class JikanEpisodeData(
    @Json(name = "mal_id") val malId: Int? = null,
    val title: String? = null,
    @Json(name = "title_japanese") val titleJapanese: String? = null,
    val aired: String? = null,
    val filler: Boolean? = false
)

@JsonClass(generateAdapter = true)
data class JikanAnimeData(
    @Json(name = "mal_id") val malId: Int,
    val title: String,
    @Json(name = "title_english") val titleEnglish: String? = null,
    @Json(name = "title_japanese") val titleJapanese: String? = null,
    val images: JikanImages? = null,
    val synopsis: String? = null,
    val episodes: Int? = null,
    val status: String? = null,
    val airing: Boolean? = false,
    val score: Double? = null,
    val rank: Int? = null,
    val popularity: Int? = null,
    val aired: JikanAired? = null,
    val season: String? = null,
    val year: Int? = null,
    val broadcast: JikanBroadcast? = null,
    val genres: List<JikanNamedItem>? = null,
    val themes: List<JikanNamedItem>? = null,
    val studios: List<JikanNamedItem>? = null,
    val relations: List<JikanRelationEntry>? = null
)

@JsonClass(generateAdapter = true)
data class JikanImages(
    val jpg: JikanImageFormat? = null,
    val webp: JikanImageFormat? = null
)

@JsonClass(generateAdapter = true)
data class JikanImageFormat(
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "small_image_url") val smallImageUrl: String? = null,
    @Json(name = "large_image_url") val largeImageUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class JikanAired(
    val string: String? = null
)

@JsonClass(generateAdapter = true)
data class JikanBroadcast(
    val day: String? = null,
    val time: String? = null,
    val timezone: String? = null,
    val string: String? = null
)

@JsonClass(generateAdapter = true)
data class JikanNamedItem(
    @Json(name = "mal_id") val malId: Int? = null,
    val name: String? = null
)

@JsonClass(generateAdapter = true)
data class JikanRelationEntry(
    val relation: String? = null,
    val entry: List<JikanRelationItem>? = null
)

@JsonClass(generateAdapter = true)
data class JikanRelationItem(
    @Json(name = "mal_id") val malId: Int? = null,
    val type: String? = null,
    val name: String? = null
)
