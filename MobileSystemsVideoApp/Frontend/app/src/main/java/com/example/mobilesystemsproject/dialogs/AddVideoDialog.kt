package com.example.mobilesystemsproject.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.mobilesystemsproject.MainActivity
import com.example.mobilesystemsproject.R
import com.example.mobilesystemsproject.RetrofitClient
import com.google.android.material.textfield.TextInputEditText
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AddVideoDialog : DialogFragment() {

    private lateinit var videoNameInput: TextInputEditText
    private lateinit var selectFileButton: Button
    private lateinit var selectedFileText: TextView
    private lateinit var uploadButton: Button
    private lateinit var cancelButton: Button
    private lateinit var progressBar: ProgressBar

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String = ""

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                selectedFileName = getFileName(uri)
                selectedFileText.text = selectedFileName
                selectedFileText.visibility = View.VISIBLE
                
                // Auto-fill video name if empty
                if (videoNameInput.text.isNullOrEmpty()) {
                    videoNameInput.setText(selectedFileName.substringBeforeLast("."))
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        videoNameInput = view.findViewById(R.id.videoNameInput)
        selectFileButton = view.findViewById(R.id.selectFileButton)
        selectedFileText = view.findViewById(R.id.selectedFileText)
        uploadButton = view.findViewById(R.id.uploadButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        progressBar = view.findViewById(R.id.progressBar)

        selectFileButton.setOnClickListener {
            openFilePicker()
        }

        uploadButton.setOnClickListener {
            uploadVideo()
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

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }

    private fun getFileName(uri: Uri): String {
        var name = "video.mp4"
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun uploadVideo() {
        val uri = selectedFileUri
        if (uri == null) {
            Toast.makeText(context, "Please select a video file", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        uploadButton.isEnabled = false
        selectFileButton.isEnabled = false

        try {
            // Copy file to cache directory
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val tempFile = File(requireContext().cacheDir, selectedFileName)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            inputStream?.close()

            // Create multipart request
            val requestFile = tempFile.asRequestBody("video/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", selectedFileName, requestFile)

            val token = RetrofitClient.getAuthToken()

            RetrofitClient.instance.uploadVideo(token, body).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    progressBar.visibility = View.GONE
                    uploadButton.isEnabled = true
                    selectFileButton.isEnabled = true

                    // Clean up temp file
                    tempFile.delete()

                    if (response.isSuccessful) {
                        Toast.makeText(context, "Video uploaded successfully!", Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.refreshHomeFragment()
                        dismiss()
                    } else {
                        Log.e("AddVideoDialog", "Upload failed: ${response.code()}")
                        Toast.makeText(context, "Upload failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    uploadButton.isEnabled = true
                    selectFileButton.isEnabled = true
                    tempFile.delete()

                    Log.e("AddVideoDialog", "Upload error: ${t.message}")
                    Toast.makeText(context, "Upload error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            uploadButton.isEnabled = true
            selectFileButton.isEnabled = true
            Log.e("AddVideoDialog", "Error: ${e.message}")
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
