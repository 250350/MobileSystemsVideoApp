package com.example.mobilesystemsproject.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilesystemsproject.R
import com.example.mobilesystemsproject.models.VideoListEntry

class VideoListAdapter(
    private val videos: MutableList<VideoListEntry>,
    private val onVideoClick: (VideoListEntry) -> Unit,
    private val onDownloadClick: (VideoListEntry) -> Unit,
    private val onShareClick: (VideoListEntry) -> Unit
) : RecyclerView.Adapter<VideoListAdapter.VideoViewHolder>() {

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.videoTitle)
        val versionText: TextView = view.findViewById(R.id.videoVersion)
        val timestampText: TextView = view.findViewById(R.id.videoTimestamp)
        val menuButton: ImageButton = view.findViewById(R.id.menuButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        Log.i("VideoListAdapter","onCreateViewHolder")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_list, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        Log.i("VideoListAdapter","onBindViewHolder pos=$position filename=${videos[position].filename} id=${videos[position].id}")
        val video = videos[position]

        holder.titleText.text = video.filename
        holder.versionText.text = "v${String.format("%.1f", video.version)}"
        holder.timestampText.text = video.timestamp?.take(10) ?: "Unknown date"

        holder.itemView.setOnClickListener {
            onVideoClick(video)
        }

        holder.menuButton.setOnClickListener { view ->
            showPopupMenu(view, video)
        }
    }

    private fun showPopupMenu(view: View, video: VideoListEntry) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.video_item_menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_download -> {
                    onDownloadClick(video)
                    true
                }
                R.id.action_share -> {
                    onShareClick(video)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    override fun getItemCount() = videos.size
}
