//package com.example.surf_club_android.ui
//
//import android.app.Activity
//import android.app.DatePickerDialog
//import android.content.Intent
//import android.graphics.Bitmap
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import com.example.surf_club_android.databinding.FragmentCreatePostBinding
//import com.example.surf_club_android.model.Model
//import com.example.surf_club_android.model.Post
//import com.example.surf_club_android.viewmodel.AuthViewModel
//import java.text.SimpleDateFormat
//import java.util.*
//
//class CreatePostFragment : Fragment() {
//
//    private var _binding: FragmentCreatePostBinding? = null
//    private val binding get() = _binding!!
//
//    private val authViewModel: AuthViewModel by viewModels()
//    private var selectedImage: Bitmap? = null
//    private var sessionDate: Date? = null
//
//    companion object {
//        private const val REQUEST_IMAGE_CAPTURE = 1
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.btnAddImage.setOnClickListener {
//            dispatchTakePictureIntent()
//        }
//
//        binding.btnCreatePost.setOnClickListener {
//            createPost()
//        }
//
//        binding.btnSelectSessionDate.setOnClickListener {
//            showDatePicker()
//        }
//
//        // Set current date for post creation
//        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//        binding.tvDate.text = "Post Date: $currentDate"
//    }
//
//    private fun showDatePicker() {
//        val calendar = Calendar.getInstance()
//        val year = calendar.get(Calendar.YEAR)
//        val month = calendar.get(Calendar.MONTH)
//        val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
//            calendar.set(selectedYear, selectedMonth, selectedDay)
//            sessionDate = calendar.time
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            binding.tvSessionDate.text = "Session Date: ${dateFormat.format(sessionDate!!)}"
//        }, year, month, day).show()
//    }
//
//    private fun dispatchTakePictureIntent() {
//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
//            }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
//            val imageBitmap = data?.extras?.get("data") as Bitmap
//            selectedImage = imageBitmap
//            binding.ivPostImage.setImageBitmap(imageBitmap)
//        }
//    }
//
//    private fun createPost() {
//        val waveHeight = binding.etWaveHeight.text.toString()
//        val windSpeed = binding.etWindSpeed.text.toString()
//        val description = binding.etDescription.text.toString()
//        val postDate = binding.tvDate.text.toString().removePrefix("Post Date: ")
//
//        if (waveHeight.isBlank() || windSpeed.isBlank() || description.isBlank() || sessionDate == null) {
//            Toast.makeText(context, "Please fill all fields and select a session date", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        showLoading(true)
//
//        val currentUserId = authViewModel.getCurrentUserId()
//        if (currentUserId == null) {
//            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
//            showLoading(false)
//            return
//        }
//
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val post = Post(
//            id = UUID.randomUUID().toString(),
//            author = currentUserId,
//            date = postDate,
//            sessionDate = dateFormat.format(sessionDate!!),
//            waveHeight = waveHeight,
//            description = description,
//            postImage = "" // This will be updated after image upload
//        )
//
//        Model.shared.addPost(post, selectedImage) {
//            showLoading(false)
//            Toast.makeText(context, "Post created successfully", Toast.LENGTH_SHORT).show()
////            navigateToHome()
//        }
//    }
//
//    private fun showLoading(isLoading: Boolean) {
//        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//        binding.btnCreatePost.isEnabled = !isLoading
//        binding.etWaveHeight.isEnabled = !isLoading
//        binding.etWindSpeed.isEnabled = !isLoading
//        binding.etDescription.isEnabled = !isLoading
//        binding.btnAddImage.isEnabled = !isLoading
//        binding.btnSelectSessionDate.isEnabled = !isLoading
//    }
//
////    private fun navigateToHome() {
////        // Assuming you're using the Navigation component
////        findNavController().navigate(R.id.action_createPostFragment_to_homeActivity)
////        // If you're not using the Navigation component, you can use this instead:
////        // startActivity(Intent(requireContext(), HomeActivity::class.java))
////        // activity?.finish()
////    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}