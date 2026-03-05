package com.example.mobilesystemsproject

import com.example.mobilesystemsproject.models.LoginRequest
import com.example.mobilesystemsproject.models.LoginResponse
import com.example.mobilesystemsproject.models.VideoListEntry
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ServiceApi {
    
    // Authentication
    @POST("api/v1/auth/register")
    fun register(@Body request: LoginRequest): Call<LoginResponse>

    @POST("api/v1/auth/authenticate")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Videos - Get user's videos (requires auth)
    @GET("api/v1/drone-recording")
    fun getMyVideos(@Header("Authorization") token: String): Call<List<VideoListEntry>>

    // Videos - Get all videos
    @GET("api/v1/drone-recording/all")
    fun getAllVideos(@Header("Authorization") token: String): Call<List<VideoListEntry>>

    // Videos - Stream/Download video by ID
    @GET("api/v1/drone-recording/{id}")
    @Streaming
    fun getVideo(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Call<ResponseBody>

    // Videos - Upload video
    @Multipart
    @POST("api/v1/drone-recording")
    fun uploadVideo(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    // Videos - Share video with user
    @POST("api/v1/drone-recording/{id}/share")
    fun shareVideo(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Query("email") email: String
    ): Call<ResponseBody>
}
