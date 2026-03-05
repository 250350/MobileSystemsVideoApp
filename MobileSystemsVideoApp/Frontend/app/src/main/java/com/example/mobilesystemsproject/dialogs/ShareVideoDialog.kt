package com.example.mobilesystemsproject.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mobilesystemsproject.R
import com.example.mobilesystemsproject.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShareVideoDialog : DialogFragment() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var shareButton: Button
    private lateinit var cancelButton: Button
    private lateinit var progressBar: ProgressBar

    private var videoId: Long = 0
    private var videoName: String = ""

    companion object {
        private const val ARG_VIDEO_ID = "video_id"
        private const val ARG_VIDEO_NAME = "video_name"

        fun newInstance(videoId: Long, videoName: String): ShareVideoDialog {
            return ShareVideoDialog().apply {
                arguments = Bundle().apply {
                    putLong(ARG_VIDEO_ID, videoId)
                    putString(ARG_VIDEO_NAME, videoName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoId = arguments?.getLong(ARG_VIDEO_ID) ?: 0
        videoName = arguments?.getString(ARG_VIDEO_NAME) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_share_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailInput = view.findViewById(R.id.emailInput)
        shareButton = view.findViewById(R.id.shareButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        progressBar = view.findViewById(R.id.progressBar)

        view.findViewById<android.widget.TextView>(R.id.dialogTitle).text = "Share \"$videoName\""

        shareButton.setOnClickListener {
            shareVideo()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun shareVideo() {
        val email = emailInput.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(context, "Please enter an email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        shareButton.isEnabled = false

        val token = RetrofitClient.getAuthToken()

        RetrofitClient.instance.shareVideo(token, videoId, email).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                progressBar.visibility = View.GONE
                shareButton.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(context, "Video shared with $email", Toast.LENGTH_SHORT).show()
                    dismiss()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("ShareVideoDialog", "Share failed: $errorBody")
                    Toast.makeText(context, "Failed to share: $errorBody", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                progressBar.visibility = View.GONE
                shareButton.isEnabled = true
                Log.e("ShareVideoDialog", "Network error: ${t.message}")
                Toast.makeText(context, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
