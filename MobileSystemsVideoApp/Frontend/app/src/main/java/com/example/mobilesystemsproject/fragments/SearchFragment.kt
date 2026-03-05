package com.example.mobilesystemsproject.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilesystemsproject.R
import com.example.mobilesystemsproject.RetrofitClient
import com.example.mobilesystemsproject.VideoDownloadManager
import com.example.mobilesystemsproject.adapters.VideoListAdapter
import com.example.mobilesystemsproject.models.VideoListEntry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private var allVideos: List<VideoListEntry> = emptyList()
    private var filteredVideos: MutableList<VideoListEntry> = mutableListOf()
    private lateinit var adapter: VideoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchView = view.findViewById(R.id.searchView)
        recyclerView = view.findViewById(R.id.searchRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyText = view.findViewById(R.id.emptyText)

        setupRecyclerView()
        setupSearchView()
        loadAllVideos()
    }

    private fun setupRecyclerView() {
        adapter = VideoListAdapter(
            videos = filteredVideos,
            onVideoClick = { video -> playVideoInPlayer(video) },
            onDownloadClick = { video -> downloadVideo(video) },
            onShareClick = { video -> showShareDialog(video) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterVideos(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterVideos(newText ?: "")
                return true
            }
        })
    }

    private fun filterVideos(query: String) {
        filteredVideos.clear()
        if (query.isEmpty()) {
            filteredVideos.addAll(allVideos)
        } else {
            filteredVideos.addAll(
                allVideos.filter { it.filename.contains(query, ignoreCase = true) }
            )
        }
        adapter.notifyDataSetChanged()

        emptyText.visibility = if (filteredVideos.isEmpty()) View.VISIBLE else View.GONE
        emptyText.text = if (query.isEmpty()) "No videos found" else "No videos matching \"$query\""
    }

    private fun loadAllVideos() {
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
                    allVideos = response.body() ?: emptyList()
                    filteredVideos.clear()
                    filteredVideos.addAll(allVideos)
                    adapter.notifyDataSetChanged()

                    if (allVideos.isEmpty()) {
                        emptyText.visibility = View.VISIBLE
                        emptyText.text = "No videos available"
                    }
                } else {
                    Log.e("SearchFragment", "Error: ${response.code()}")
                    emptyText.visibility = View.VISIBLE
                    emptyText.text = "Failed to load videos"
                }
            }

            override fun onFailure(call: Call<List<VideoListEntry>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("SearchFragment", "Network error: ${t.message}")
                emptyText.visibility = View.VISIBLE
                emptyText.text = "Network error"
            }
        })
    }

    private fun playVideoInPlayer(video: VideoListEntry) {
        // Navigate to a video player or play in a dialog
        Toast.makeText(context, "Playing: ${video.filename}", Toast.LENGTH_SHORT).show()
        // For now, we can navigate to home and play there
        // Or implement a full-screen player
    }

    private fun downloadVideo(video: VideoListEntry) {
        Toast.makeText(context, "Downloading ${video.filename}...", Toast.LENGTH_SHORT).show()
        VideoDownloadManager.downloadVideo(requireContext(), video)
    }

    private fun showShareDialog(video: VideoListEntry) {
        val dialog = com.example.mobilesystemsproject.dialogs.ShareVideoDialog.newInstance(video.id, video.filename)
        dialog.show(parentFragmentManager, "ShareVideoDialog")
    }
}
