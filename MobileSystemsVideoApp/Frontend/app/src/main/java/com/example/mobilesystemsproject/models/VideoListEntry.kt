package com.example.mobilesystemsproject.models

import com.google.gson.annotations.SerializedName

data class VideoListEntry(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("version")
    val version: Double,
    
    @SerializedName("filename")
    val filename: String,
    
    @SerializedName("timestamp")
    val timestamp: String?
)
