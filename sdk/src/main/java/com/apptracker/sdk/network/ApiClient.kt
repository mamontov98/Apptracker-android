 package com.apptracker.sdk.network

import com.apptracker.sdk.Event
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Request model for batch events API
data class BatchEventsRequest(
    val projectKey: String,
    val events: List<Event>
)

// Response model for batch events API
data class BatchEventsResponse(
    val received: Int,
    val inserted: Int
)

// Request model for creating a project
data class CreateProjectRequest(
    val name: String
)

// Response model for project creation
data class ProjectResponse(
    val name: String,
    val projectKey: String,
    val createdAt: String,
    val isActive: Boolean
)

// Response model for getting projects
data class ProjectsResponse(
    val projects: List<ProjectResponse>
)

// Retrofit interface for AppTracker API
interface AppTrackerApi {
    @POST("/v1/events/batch")
    suspend fun sendBatchEvents(@Body request: BatchEventsRequest): Response<BatchEventsResponse>
    
    @GET("/v1/projects")
    suspend fun getProjects(
        @Query("projectKey") projectKey: String? = null,
        @Query("name") name: String? = null
    ): Response<ProjectsResponse>
    
    @POST("/v1/projects")
    suspend fun createProject(@Body request: CreateProjectRequest): Response<ProjectResponse>
}

// Factory for creating API client instances
object ApiClient {
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    fun create(baseUrl: String, enableLogging: Boolean = false): AppTrackerApi {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            httpClient.addInterceptor(loggingInterceptor)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl.ensureTrailingSlash())
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(AppTrackerApi::class.java)
    }

    private fun String.ensureTrailingSlash(): String {
        return if (this.endsWith("/")) this else "$this/"
    }
}

