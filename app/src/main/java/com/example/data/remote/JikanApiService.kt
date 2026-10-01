package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface JikanApiService {
    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("sfw") sfw: Boolean = true
    ): JikanListResponse

    @GET("top/anime")
    suspend fun getTopAnime(
        @Query("filter") filter: String? = null,
        @Query("limit") limit: Int = 25
    ): JikanListResponse

    @GET("seasons/now")
    suspend fun getSeasonsNow(
        @Query("limit") limit: Int = 25
    ): JikanListResponse

    @GET("seasons/upcoming")
    suspend fun getSeasonsUpcoming(
        @Query("limit") limit: Int = 25
    ): JikanListResponse

    @GET("schedules")
    suspend fun getSchedules(
        @Query("filter") day: String? = null,
        @Query("sfw") sfw: Boolean = true,
        @Query("limit") limit: Int = 25
    ): JikanListResponse

    @GET("anime/{id}")
    suspend fun getAnimeDetails(
        @Path("id") id: Int
    ): JikanSingleResponse

    @GET("anime/{id}/full")
    suspend fun getAnimeFull(
        @Path("id") id: Int
    ): JikanSingleResponse

    @GET("anime/{id}/episodes")
    suspend fun getAnimeEpisodes(
        @Path("id") id: Int
    ): JikanEpisodesResponse

    @GET("random/anime")
    suspend fun getRandomAnime(): JikanSingleResponse
}

class JikanRateLimitAndRetryInterceptor : Interceptor {
    private val lock = Any()
    private var lastRequestTimestamp = 0L

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 1. Enforce minimum delay of 350ms between outgoing requests to avoid Jikan 429 rate limit
        synchronized(lock) {
            val now = System.currentTimeMillis()
            val timeSinceLast = now - lastRequestTimestamp
            if (timeSinceLast < 350) {
                try {
                    Thread.sleep(350 - timeSinceLast)
                } catch (_: InterruptedException) {
                    // Ignore interruption
                }
            }
            lastRequestTimestamp = System.currentTimeMillis()
        }

        // 2. Attach proper User-Agent and headers
        val newRequest = originalRequest.newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile; en-US) AnimeVault/2.0")
            .header("Accept", "application/json")
            .build()

        var response = chain.proceed(newRequest)

        // 3. Retry on HTTP 429 Too Many Requests
        var attempts = 0
        while (response.code == 429 && attempts < 2) {
            attempts++
            response.close()
            try {
                Thread.sleep(1200)
            } catch (_: InterruptedException) {
                break
            }
            response = chain.proceed(newRequest)
        }

        return response
    }
}

object NetworkClient {
    private const val BASE_URL = "https://api.jikan.moe/v4/"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .addInterceptor(JikanRateLimitAndRetryInterceptor())
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val jikanApi: JikanApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(JikanApiService::class.java)
    }
}
