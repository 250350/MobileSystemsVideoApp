package com.example.mobilesystemsproject.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilesystemsproject.R
import com.example.mobilesystemsproject.RetrofitClient
import com.example.mobilesystemsproject.TokenManager
import com.example.mobilesystemsproject.VideoDownloadManager
import com.example.mobilesystemsproject.adapters.VideoListAdapter
import com.example.mobilesystemsproject.models.VideoListEntry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var playerView: androidx.media3.ui.PlayerView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var featuredTitle: TextView
    private var player: androidx.media3.exoplayer.ExoPlayer? = null
    private var videoList: MutableList<VideoListEntry> = mutableListOf()
    private lateinit var adapter: VideoListAdapter
    private var currentFeaturedVideo: VideoListEntry? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playerView = view.findViewById(R.id.playerView)
        recyclerView = view.findViewById(R.id.videoRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyText = view.findViewById(R.id.emptyText)
        featuredTitle = view.findViewById(R.id.featuredTitle)

        setupPlayer()
        setupRecyclerView()
        loadVideos()
    }

    private fun setupPlayer() {
        player = androidx.media3.exoplayer.ExoPlayer.Builder(requireContext()).build()
        playerView.player = player

        player?.addListener(object : androidx.media3.common.Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e("HomeFragment", "Player error: ${error.message}, type: ${error.errorCodeName}")
            }

            override fun onPlaybackStateChanged(state: Int) {
                val stateStr = when(state) {
                    androidx.media3.common.Player.STATE_IDLE -> "IDLE"
                    androidx.media3.common.Player.STATE_BUFFERING -> "BUFFERING"
                    androidx.media3.common.Player.STATE_READY -> "READY"
                    androidx.media3.common.Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                Log.d("HomeFragment", "Player state: $stateStr")
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = VideoListAdapter(
            videos = videoList,
            onVideoClick = { video -> playVideo(video) },
            onDownloadClick = { video -> downloadVideo(video) },
            onShareClick = { video -> showShareDialog(video) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    fun refreshVideos() {
        loadVideos()
    }

    private fun loadVideos() {
        progressBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE

        val token = RetrofitClient.getAuthToken()

        RetrofitClient.instance.getMyVideos(token).enqueue(object : Callback<List<VideoListEntry>> {
            override fun onResponse(
                call: Call<List<VideoListEntry>>,
                response: Response<List<VideoListEntry>>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val videos = response.body() ?: emptyList()
                    videoList.clear()
                    videoList.addAll(videos)
                    adapter.notifyDataSetChanged()

                    if (videos.isEmpty()) {
                        emptyText.visibility = View.VISIBLE
                        playerView.visibility = View.GONE
                        featuredTitle.visibility = View.GONE
                    } else {
                        emptyText.visibility = View.GONE
                        playerView.visibility = View.VISIBLE
                        featuredTitle.visibility = View.VISIBLE
                        // Play random video as featured
                        val randomVideo = videos.random()
                        currentFeaturedVideo = randomVideo
                        featuredTitle.text = "Now Playing: ${randomVideo.filename}"
                        playVideo(randomVideo)
                    }
                } else {
                    Log.e("HomeFragment", "Error loading videos: ${response.code()}")
                    Toast.makeText(context, "Failed to load videos", Toast.LENGTH_SHORT).show()
                    emptyText.visibility = View.VISIBLE
                    emptyText.text = "Failed to load videos"
                }
            }

            override fun onFailure(call: Call<List<VideoListEntry>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("HomeFragment", "Network error: ${t.message}")
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                emptyText.visibility = View.VISIBLE
                emptyText.text = "Network error"
            }
        })
    }

    @OptIn(UnstableApi::class)
    private fun playVideo(video: VideoListEntry) {
        currentFeaturedVideo = video
        featuredTitle.text = "Now Playing: ${video.filename}"
        Log.e("DEBUG", "playVideo called with id=${video.id}")

        val host = "192.168.1.135:8080"
        val videoUrl = "http://$host/api/v1/drone-recording/${video.id}"
        val token = RetrofitClient.getAuthToken()

        val dataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(mapOf("Authorization" to token))

        val mediaItem = androidx.media3.common.MediaItem.fromUri(videoUrl.toUri())
        val mediaSource = androidx.media3.exoplayer.source.ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)

        player?.apply {
            //stop current video and clear old MediaSource
            stop()
            clearMediaItems()

            //Add new video
            setMediaSource(mediaSource)
            prepare()
            play()
        }

        Log.d("HomeFragment", "Now playing video: ${video.filename}, URL: $videoUrl")
    }

    private fun downloadVideo(video: VideoListEntry) {
        Toast.makeText(context, "Downloading ${video.filename}...", Toast.LENGTH_SHORT).show()
        // Download implementation will be in VideoDownloadManager
        VideoDownloadManager.downloadVideo(requireContext(), video)
    }

    private fun showShareDialog(video: VideoListEntry) {
        val dialog = com.example.mobilesystemsproject.dialogs.ShareVideoDialog.newInstance(video.id, video.filename)
        dialog.show(parentFragmentManager, "ShareVideoDialog")
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
    }
}