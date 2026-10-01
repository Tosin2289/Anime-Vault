package com.example.data.remote

import android.util.Log
import com.example.data.model.AnimeItem
import com.example.data.model.AnimeRelation
import com.example.data.model.AnimeReleaseItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object AniListApiClient {
    private const val GRAPHQL_ENDPOINT = "https://graphql.anilist.co"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    private suspend fun executeQuery(query: String, variables: JSONObject = JSONObject()): JSONObject? =
        withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("query", query)
                    put("variables", variables)
                }

                val body = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
                val request = Request.Builder()
                    .url(GRAPHQL_ENDPOINT)
                    .post(body)
                    .header("User-Agent", "Mozilla/5.0 (AnimeVault Android)")
                    .header("Accept", "application/json")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (!responseBody.isNullOrBlank()) {
                        return@withContext JSONObject(responseBody)
                    }
                } else {
                    Log.w("AniListApiClient", "AniList error HTTP ${response.code}: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("AniListApiClient", "GraphQL query failed: ${e.message}")
            }
            null
        }

    suspend fun getTrendingAnime(limit: Int = 20): List<AnimeItem> = withContext(Dispatchers.IO) {
        val query = """
            query(${'$'}limit: Int) {
                Page(page: 1, perPage: ${'$'}limit) {
                    media(type: ANIME, sort: TRENDING_DESC) {
                        id
                        idMal
                        title { romaji english native }
                        coverImage { large extraLarge }
                        bannerImage
                        description(asHtml: false)
                        episodes
                        status
                        averageScore
                        popularity
                        season
                        seasonYear
                        genres
                        studios(isMain: true) { nodes { name } }
                        nextAiringEpisode { episode airingAt timeUntilAiring }
                        relations { edges { relationType node { id idMal title { romaji english } } } }
                    }
                }
            }
        """.trimIndent()

        val variables = JSONObject().put("limit", limit)
        val json = executeQuery(query, variables) ?: return@withContext emptyList()
        parseMediaList(json)
    }

    suspend fun getCurrentlyAiring(limit: Int = 20): List<AnimeItem> = withContext(Dispatchers.IO) {
        val query = """
            query(${'$'}limit: Int) {
                Page(page: 1, perPage: ${'$'}limit) {
                    media(type: ANIME, status: RELEASING, sort: POPULARITY_DESC) {
                        id
                        idMal
                        title { romaji english native }
                        coverImage { large extraLarge }
                        bannerImage
                        description(asHtml: false)
                        episodes
                        status
                        averageScore
                        popularity
                        season
                        seasonYear
                        genres
                        studios(isMain: true) { nodes { name } }
                        nextAiringEpisode { episode airingAt timeUntilAiring }
                        relations { edges { relationType node { id idMal title { romaji english } } } }
                    }
                }
            }
        """.trimIndent()

        val variables = JSONObject().put("limit", limit)
        val json = executeQuery(query, variables) ?: return@withContext emptyList()
        parseMediaList(json)
    }

    suspend fun getUpcomingReleases(limit: Int = 20): List<AnimeItem> = withContext(Dispatchers.IO) {
        val query = """
            query(${'$'}limit: Int) {
                Page(page: 1, perPage: ${'$'}limit) {
                    media(type: ANIME, status: NOT_YET_RELEASED, sort: POPULARITY_DESC) {
                        id
                        idMal
                        title { romaji english native }
                        coverImage { large extraLarge }
                        bannerImage
                        description(asHtml: false)
                        episodes
                        status
                        averageScore
                        popularity
                        season
                        seasonYear
                        genres
                        studios(isMain: true) { nodes { name } }
                        nextAiringEpisode { episode airingAt timeUntilAiring }
                        relations { edges { relationType node { id idMal title { romaji english } } } }
                    }
                }
            }
        """.trimIndent()

        val variables = JSONObject().put("limit", limit)
        val json = executeQuery(query, variables) ?: return@withContext emptyList()
        parseMediaList(json)
    }

    suspend fun getAiringReleases(limit: Int = 20): List<AnimeReleaseItem> = withContext(Dispatchers.IO) {
        val query = """
            query(${'$'}limit: Int) {
                Page(page: 1, perPage: ${'$'}limit) {
                    airingSchedules(notYetAired: true, sort: TIME) {
                        id
                        airingAt
                        timeUntilAiring
                        episode
                        media {
                            id
                            idMal
                            title { romaji english }
                            coverImage { large extraLarge }
                            format
                            genres
                        }
                    }
                }
            }
        """.trimIndent()

        val variables = JSONObject().put("limit", limit)
        val json = executeQuery(query, variables) ?: return@withContext emptyList()
        parseAiringReleases(json)
    }

    suspend fun searchAnime(searchQuery: String, limit: Int = 20): List<AnimeItem> = withContext(Dispatchers.IO) {
        val query = """
            query(${'$'}search: String, ${'$'}limit: Int) {
                Page(page: 1, perPage: ${'$'}limit) {
                    media(type: ANIME, search: ${'$'}search, sort: POPULARITY_DESC) {
                        id
                        idMal
                        title { romaji english native }
                        coverImage { large extraLarge }
                        bannerImage
                        description(asHtml: false)
                        episodes
                        status
                        averageScore
                        popularity
                        season
                        seasonYear
                        genres
                        studios(isMain: true) { nodes { name } }
                        nextAiringEpisode { episode airingAt timeUntilAiring }
                        relations { edges { relationType node { id idMal title { romaji english } } } }
                    }
                }
            }
        """.trimIndent()

        val variables = JSONObject().apply {
            put("search", searchQuery)
            put("limit", limit)
        }
        val json = executeQuery(query, variables) ?: return@withContext emptyList()
        parseMediaList(json)
    }

    suspend fun getAnimeDetails(malId: Int): AnimeItem? = withContext(Dispatchers.IO) {
        val query = """
            query(${'$'}idMal: Int) {
                Media(idMal: ${'$'}idMal, type: ANIME) {
                    id
                    idMal
                    title { romaji english native }
                    coverImage { large extraLarge }
                    bannerImage
                    description(asHtml: false)
                    episodes
                    status
                    averageScore
                    popularity
                    season
                    seasonYear
                    genres
                    studios(isMain: true) { nodes { name } }
                    nextAiringEpisode { episode airingAt timeUntilAiring }
                    relations { edges { relationType node { id idMal title { romaji english } } } }
                }
            }
        """.trimIndent()

        val variables = JSONObject().put("idMal", malId)
        val json = executeQuery(query, variables) ?: return@withContext null
        val data = json.optJSONObject("data") ?: return@withContext null
        val mediaObj = data.optJSONObject("Media") ?: return@withContext null
        parseSingleMedia(mediaObj)
    }

    private fun parseMediaList(json: JSONObject): List<AnimeItem> {
        val list = mutableListOf<AnimeItem>()
        try {
            val data = json.optJSONObject("data") ?: return emptyList()
            val page = data.optJSONObject("Page") ?: return emptyList()
            val mediaArray = page.optJSONArray("media") ?: return emptyList()

            for (i in 0 until mediaArray.length()) {
                val mediaObj = mediaArray.getJSONObject(i)
                parseSingleMedia(mediaObj)?.let { list.add(it) }
            }
        } catch (e: Exception) {
            Log.e("AniListApiClient", "Error parsing media list: ${e.message}")
        }
        return list
    }

    private fun parseSingleMedia(obj: JSONObject): AnimeItem? {
        try {
            val aniListId = obj.optInt("id")
            val idMal = if (obj.has("idMal") && !obj.isNull("idMal")) obj.getInt("idMal") else aniListId
            if (idMal <= 0 && aniListId <= 0) return null

            val titleObj = obj.optJSONObject("title")
            val romajiTitle = titleObj?.optString("romaji") ?: ""
            val englishTitle = titleObj?.optString("english")
            val nativeTitle = titleObj?.optString("native")
            val primaryTitle = if (!englishTitle.isNullOrBlank()) englishTitle else romajiTitle

            val coverObj = obj.optJSONObject("coverImage")
            val imageUrl = coverObj?.optString("extraLarge")?.takeIf { it.isNotBlank() }
                ?: coverObj?.optString("large")?.takeIf { it.isNotBlank() }
                ?: "https://cdn.myanimelist.net/images/anime/1015/138075l.jpg"

            val bannerUrl = obj.optString("bannerImage").takeIf { it.isNotBlank() }
            val rawDesc = obj.optString("description")
            val synopsis = cleanDescription(rawDesc)

            val episodesTotal = if (obj.has("episodes") && !obj.isNull("episodes")) obj.getInt("episodes") else null
            val statusRaw = obj.optString("status")
            val isAiring = statusRaw == "RELEASING"
            val airingStatus = when (statusRaw) {
                "RELEASING" -> "Currently Airing"
                "NOT_YET_RELEASED" -> "Not Yet Aired"
                "FINISHED" -> "Finished Airing"
                else -> "Finished Airing"
            }

            val avgScore = if (obj.has("averageScore") && !obj.isNull("averageScore")) {
                obj.getDouble("averageScore") / 10.0
            } else null

            val popularity = if (obj.has("popularity") && !obj.isNull("popularity")) obj.getInt("popularity") else null
            val season = obj.optString("season").takeIf { it.isNotBlank() }?.lowercase()?.replaceFirstChar { it.uppercase() }
            val seasonYear = if (obj.has("seasonYear") && !obj.isNull("seasonYear")) obj.getInt("seasonYear") else null

            // Genres
            val genres = mutableListOf<String>()
            val genresArr = obj.optJSONArray("genres")
            if (genresArr != null) {
                for (g in 0 until genresArr.length()) {
                    genres.add(genresArr.getString(g))
                }
            }

            // Studios
            val studios = mutableListOf<String>()
            val studiosObj = obj.optJSONObject("studios")
            val nodesArr = studiosObj?.optJSONArray("nodes")
            if (nodesArr != null) {
                for (s in 0 until nodesArr.length()) {
                    val node = nodesArr.getJSONObject(s)
                    val name = node.optString("name")
                    if (name.isNotBlank()) studios.add(name)
                }
            }

            // Broadcast Day from next airing episode
            var broadcastDay: String? = null
            var broadcastTime: String? = null
            val nextEp = obj.optJSONObject("nextAiringEpisode")
            if (nextEp != null && nextEp.has("airingAt") && !nextEp.isNull("airingAt")) {
                val airingAtSec = nextEp.getLong("airingAt")
                val date = Date(airingAtSec * 1000)
                broadcastDay = SimpleDateFormat("EEEE", Locale.ENGLISH).format(date)
                broadcastTime = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(date)
            }

            // Relations
            val relations = mutableListOf<AnimeRelation>()
            val relObj = obj.optJSONObject("relations")
            val edgesArr = relObj?.optJSONArray("edges")
            if (edgesArr != null) {
                for (r in 0 until edgesArr.length()) {
                    val edge = edgesArr.getJSONObject(r)
                    val relType = edge.optString("relationType")
                    val node = edge.optJSONObject("node")
                    if (node != null) {
                        val relId = if (node.has("idMal") && !node.isNull("idMal")) node.getInt("idMal") else node.optInt("id")
                        val nodeTitleObj = node.optJSONObject("title")
                        val nodeTitle = nodeTitleObj?.optString("english")?.takeIf { it.isNotBlank() }
                            ?: nodeTitleObj?.optString("romaji") ?: "Sequel"
                        relations.add(
                            AnimeRelation(
                                relationType = relType,
                                malId = relId,
                                title = nodeTitle,
                                type = "Anime"
                            )
                        )
                    }
                }
            }

            return AnimeItem(
                malId = if (idMal > 0) idMal else aniListId,
                title = primaryTitle,
                englishTitle = englishTitle,
                japaneseTitle = nativeTitle,
                imageUrl = imageUrl,
                bannerUrl = bannerUrl,
                synopsis = synopsis,
                episodesTotal = episodesTotal,
                score = avgScore,
                popularity = popularity,
                airingStatus = airingStatus,
                isAiring = isAiring,
                broadcastDay = broadcastDay,
                broadcastTime = broadcastTime,
                season = season,
                year = seasonYear,
                genres = genres,
                studios = studios,
                relations = relations
            )
        } catch (e: Exception) {
            Log.e("AniListApiClient", "Error mapping single media: ${e.message}")
            return null
        }
    }

    private fun parseAiringReleases(json: JSONObject): List<AnimeReleaseItem> {
        val releases = mutableListOf<AnimeReleaseItem>()
        try {
            val data = json.optJSONObject("data") ?: return emptyList()
            val page = data.optJSONObject("Page") ?: return emptyList()
            val schedArr = page.optJSONArray("airingSchedules") ?: return emptyList()

            for (i in 0 until schedArr.length()) {
                val item = schedArr.getJSONObject(i)
                val id = item.optInt("id")
                val airingAt = item.optLong("airingAt")
                val timeUntil = item.optLong("timeUntilAiring")
                val episode = item.optInt("episode")

                val media = item.optJSONObject("media") ?: continue
                val aniId = media.optInt("id")
                val malId = if (media.has("idMal") && !media.isNull("idMal")) media.getInt("idMal") else aniId
                val titleObj = media.optJSONObject("title")
                val title = titleObj?.optString("english")?.takeIf { it.isNotBlank() }
                    ?: titleObj?.optString("romaji") ?: "Anime #$malId"

                val coverObj = media.optJSONObject("coverImage")
                val img = coverObj?.optString("large") ?: ""
                val format = media.optString("format", "TV")

                val genres = mutableListOf<String>()
                val gArr = media.optJSONArray("genres")
                if (gArr != null) {
                    for (g in 0 until gArr.length()) {
                        genres.add(gArr.getString(g))
                    }
                }

                releases.add(
                    AnimeReleaseItem(
                        id = id,
                        malId = if (malId > 0) malId else aniId,
                        title = title,
                        imageUrl = img,
                        episodeNumber = episode,
                        airingAt = airingAt,
                        timeUntilAiringSeconds = timeUntil,
                        format = format,
                        genres = genres
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("AniListApiClient", "Error parsing airing releases: ${e.message}")
        }
        return releases
    }

    private fun cleanDescription(raw: String): String {
        return raw.replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]*>"), "")
            .replace("&#039;", "'")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .trim()
    }
}
