package com.example.surf_club_android.view.fragments

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.surf_club_android.R
import com.example.surf_club_android.databinding.FragmentUpdatePostBinding
import com.example.surf_club_android.model.Model
import com.example.surf_club_android.model.Post
import java.text.SimpleDateFormat
import java.util.*

class UpdatePostFragment : Fragment() {

    private var _binding: FragmentUpdatePostBinding? = null
    private val binding get() = _binding!!

    // Using Safe Args. Adjust type if needed.
    private val args: UpdatePostFragment by navArgs()
    private lateinit var post: Post
    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivPostImage.setImageURI(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUpdatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        post = args.post
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        binding.apply {
            tvDate.text = getString(R.string.post_date, post.date)
            tvSessionDate.text = getString(R.string.session_date, post.sessionDate)
            etWaveHeight.setText(post.waveHeight)
            etWindSpeed.setText(post.windSpeed)
            etDescription.setText(post.description)
            Glide.with(requireContext())
                .load(post.postImage)
                .placeholder(R.drawable.ic_upload_photo)
                .into(ivPostImage)
            // Display the Post ID in the newly added TextView.
            tvPostId.text = "Post ID: ${post.id}"
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnUpdateImage.setOnClickListener {
                getContent.launch("image/*")
            }

            btnUpdateSessionDate.setOnClickListener {
                showDatePicker()
            }

            btnUpdatePost.setOnClickListener {
                updatePost()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(calendar.time)
                binding.tvSessionDate.text = getString(R.string.session_date, formattedDate)
                post = post.copy(sessionDate = formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updatePost() {
        binding.apply {
            val updatedPost = post.copy(
                waveHeight = etWaveHeight.text.toString(),
                windSpeed = etWindSpeed.text.toString(),
                description = etDescription.text.toString()
            )
            progressBar.visibility = View.VISIBLE
            btnUpdatePost.isEnabled = false

            // Convert the selected image URI to a Bitmap if available.
            val bitmap: Bitmap? = selectedImageUri?.let { uri -> getBitmapFromUri(uri) }

            Model.shared.updatePost(updatedPost, bitmap) { success ->
                progressBar.visibility = View.GONE
                btnUpdatePost.isEnabled = true
                if (success) {
                    Toast.makeText(requireContext(), "Post updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), "Failed to update post", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
