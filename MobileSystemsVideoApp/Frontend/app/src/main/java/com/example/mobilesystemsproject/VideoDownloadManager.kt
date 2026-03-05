package com.example.mobilesystemsproject

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.mobilesystemsproject.models.VideoListEntry
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object VideoDownloadManager {

    fun downloadVideo(context: Context, video: VideoListEntry) {
        val token = RetrofitClient.getAuthToken()

        RetrofitClient.instance.getVideo(token, video.id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        saveVideoToStorage(context, body, video.filename)
                    } ?: run {
                        Toast.makeText(context, "Download failed: Empty response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("VideoDownload", "Download failed: ${response.code()}")
                    Toast.makeText(context, "Download failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("VideoDownload", "Download error: ${t.message}")
                Toast.makeText(context, "Download error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveVideoToStorage(context: Context, body: ResponseBody, filename: String) {
        try {
            val outputStream: OutputStream?
            val savedPath: String

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Use MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/VideoApp")
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (uri == null) {
                    Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show()
                    return
                }

                outputStream = context.contentResolver.openOutputStream(uri)
                savedPath = "Movies/VideoApp/$filename"
            } else {
                // Android 9 and below
                val moviesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "VideoApp"
                )
                if (!moviesDir.exists()) {
                    moviesDir.mkdirs()
                }

                val file = File(moviesDir, filename)
                outputStream = FileOutputStream(file)
                savedPath = file.absolutePath
            }

            outputStream?.use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }

            Toast.makeText(context, "Downloaded to: $savedPath", Toast.LENGTH_LONG).show()
            Log.i("VideoDownload", "Video saved to: $savedPath")

        } catch (e: Exception) {
            Log.e("VideoDownload", "Save error: ${e.message}")
            Toast.makeText(context, "Save error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
