package com.example.surf_club_android.view.fragments

import android.annotation.SuppressLint
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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.surf_club_android.R
import com.example.surf_club_android.databinding.FragmentCreatePostBinding
import com.example.surf_club_android.model.repositories.UserRepository
import com.example.surf_club_android.model.repositories.PostRepository
import com.example.surf_club_android.model.schemas.Post
import com.example.surf_club_android.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

class
CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var sessionDate: Date? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivPostImage.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddImage.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.btnCreatePost.setOnClickListener {
            createPost()
        }

        binding.btnSelectSessionDate.setOnClickListener {
            showDatePicker()
        }

        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        binding.tvDate.text = "Post Date: $currentDate"
    }

    @SuppressLint("SetTextI18n")
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            sessionDate = calendar.time
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            binding.tvSessionDate.text = "Session Date: ${dateFormat.format(sessionDate!!)}"
        }, year, month, day).show()
    }

    private fun createPost() {
        val waveHeight = binding.etWaveHeight.text.toString()
        val windSpeed = binding.etWindSpeed.text.toString()
        val description = binding.etDescription.text.toString()
        val postDate = binding.tvDate.text.toString().removePrefix("Post Date: ")

        if (waveHeight.isBlank() || windSpeed.isBlank() || description.isBlank() || sessionDate == null) {
            Toast.makeText(context, "Please fill all fields and select a session date", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        val currentUserId = authViewModel.getCurrentUserId()
        if (currentUserId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            showLoading(false)
            return
        }

        // Check the user's role before creating the post.
        UserRepository.shared.getUser(currentUserId) { user ->
            if (user == null || user.role != "מדריך") {
                activity?.runOnUiThread {
                    showLoading(false)
                    Toast.makeText(context, "Only instructors allowed to upload posts", Toast.LENGTH_LONG).show()
                }
                return@getUser
            }

            // Role is "מדריך"; proceed to create the post.
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val post = Post(
                id = UUID.randomUUID().toString(),
                author = currentUserId,
                date = postDate,
                sessionDate = dateFormat.format(sessionDate!!),
                waveHeight = waveHeight,
                windSpeed = windSpeed,
                description = description,
                postImage = ""
            )

            val bitmap: Bitmap? = selectedImageUri?.let { getBitmapFromUri(it) }

            PostRepository.shared.addPost(post, bitmap) { success, _ ->
                activity?.runOnUiThread {
                    showLoading(false)
                    if (success) {
                        Toast.makeText(context, "Post created successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_createPostFragment_to_homeFragment)
                    } else {
                        Toast.makeText(context, "Failed to create post", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnCreatePost.isEnabled = !isLoading
        binding.etWaveHeight.isEnabled = !isLoading
        binding.etWindSpeed.isEnabled = !isLoading
        binding.etDescription.isEnabled = !isLoading
        binding.btnAddImage.isEnabled = !isLoading
        binding.btnSelectSessionDate.isEnabled = !isLoading
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
