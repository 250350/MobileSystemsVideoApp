package com.example.mobilesystemsproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mobilesystemsproject.MainActivity
import com.example.mobilesystemsproject.R
import com.example.mobilesystemsproject.RetrofitClient
import com.example.mobilesystemsproject.models.VideoListEntry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CloudFragment : Fragment() {

    private lateinit var syncButton: Button
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var videoCountText: TextView
    private lateinit var lastSyncText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cloud, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        syncButton = view.findViewById(R.id.syncButton)
        statusText = view.findViewById(R.id.statusText)
        progressBar = view.findViewById(R.id.progressBar)
        videoCountText = view.findViewById(R.id.videoCountText)
        lastSyncText = view.findViewById(R.id.lastSyncText)

        syncButton.setOnClickListener {
            syncWithCloud()
        }

        // Initial sync status
        checkCloudStatus()
    }

    private fun checkCloudStatus() {
        progressBar.visibility = View.VISIBLE
        statusText.text = "Checking cloud connection..."

        val token = RetrofitClient.getAuthToken()

        RetrofitClient.instance.getMyVideos(token).enqueue(object : Callback<List<VideoListEntry>> {
            override fun onResponse(
                call: Call<List<VideoListEntry>>,
                response: Response<List<VideoListEntry>>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val videos = response.body() ?: emptyList()
                    statusText.text = "Connected to cloud"
                    statusText.setTextColor(resources.getColor(android.R.color.holo_green_light, null))
                    videoCountText.text = "Videos in cloud: ${videos.size}"
                    lastSyncText.text = "Last checked: Just now"
                } else {
                    statusText.text = "Connection failed (${response.code()})"
                    statusText.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
                }
            }

            override fun onFailure(call: Call<List<VideoListEntry>>, t: Throwable) {
                progressBar.visibility = View.GONE
                statusText.text = "Connection error"
                statusText.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
                videoCountText.text = "Unable to reach cloud"
            }
        })
    }

    private fun syncWithCloud() {
        progressBar.visibility = View.VISIBLE
        syncButton.isEnabled = false
        statusText.text = "Syncing with cloud..."

        val token = RetrofitClient.getAuthToken()

        RetrofitClient.instance.getMyVideos(token).enqueue(object : Callback<List<VideoListEntry>> {
            override fun onResponse(
                call: Call<List<VideoListEntry>>,
                response: Response<List<VideoListEntry>>
            ) {
                progressBar.visibility = View.GONE
                syncButton.isEnabled = true

                if (response.isSuccessful) {
                    val videos = response.body() ?: emptyList()
                    statusText.text = "Sync complete!"
                    statusText.setTextColor(resources.getColor(android.R.color.holo_green_light, null))
                    videoCountText.text = "Videos in cloud: ${videos.size}"
                    lastSyncText.text = "Last synced: Just now"
                    
                    Toast.makeText(context, "Sync complete! ${videos.size} videos found.", Toast.LENGTH_SHORT).show()
                    
                    // Refresh home fragment
                    (activity as? MainActivity)?.refreshHomeFragment()
                } else {
                    statusText.text = "Sync failed (${response.code()})"
                    statusText.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
                    Toast.makeText(context, "Sync failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<VideoListEntry>>, t: Throwable) {
                progressBar.visibility = View.GONE
                syncButton.isEnabled = true
                statusText.text = "Sync error"
                statusText.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
