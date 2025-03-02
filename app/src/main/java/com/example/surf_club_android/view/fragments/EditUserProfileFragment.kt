package com.example.surf_club_android.view.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.surf_club_android.R
import com.example.surf_club_android.databinding.FragmentEditUserProfileBinding
import com.example.surf_club_android.model.CloudinaryModel
import com.example.surf_club_android.model.User
import com.example.surf_club_android.viewmodel.EditUserProfileViewModel


class EditUserProfileFragment : Fragment() {
    private var _binding: FragmentEditUserProfileBinding? = null
    private val binding: FragmentEditUserProfileBinding
        get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreateView and onDestroyView")

    private val viewModel: EditUserProfileViewModel by viewModels()
    private var selectedImage: Bitmap? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                @Suppress("DEPRECATION")
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                binding.ivProfile.setImageBitmap(bitmap)
                selectedImage = bitmap
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUserProfileBinding.inflate(inflater, container, false)
        return _binding?.root ?: throw IllegalStateException("Binding cannot be null")
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let { updateUI(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, R.string.profile_updated_successfully, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
        setupRoleDropdown()
        viewModel.loadUserData()
    }

    private fun setupRoleDropdown() {
        val roles = arrayOf("מדריך", "מתנדב", "בית הלוחם ת''א", "בית הלוחם ירושלים", "אקי''ם", "הצעד הבא")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRole.setAdapter(adapter)
        binding.actvRole.setOnClickListener { binding.actvRole.showDropDown() }
    }


    private fun setupListeners() {
        binding.btnEdit.setOnClickListener {
            val updatedUser = User(
                id = viewModel.user.value?.id.orEmpty(),
                firstName = binding.etFirstName.text.toString(),
                lastName = binding.etLastName.text.toString(),
                email = binding.etEmail.text.toString(),
                role = binding.actvRole.text.toString(),
                aboutMe = binding.etAboutMe.text.toString(),
                profileImageUrl = viewModel.user.value?.profileImageUrl.orEmpty()
            )

            if (selectedImage != null) {
                uploadImageToCloudinary(selectedImage!!, updatedUser)
            } else {
                viewModel.updateUserProfile(updatedUser, null)
            }
        }

        binding.tvEditPhoto.setOnClickListener {
            pickImage.launch("image/*")
        }
    }    private fun updateUI(user: User) {
        binding.etFirstName.setText(user.firstName)
        binding.etLastName.setText(user.lastName)
        binding.etEmail.setText(user.email)
        binding.actvRole.text.toString()
        binding.etAboutMe.setText(user.aboutMe)

        Glide.with(this)
            .load(user.profileImageUrl)
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(binding.ivProfile)
    }

    private fun uploadImageToCloudinary(bitmap: Bitmap, updatedUser: User) {
        val cloudinaryModel = CloudinaryModel()
        val file = cloudinaryModel.bitmapToFile(bitmap, requireContext())

        MediaManager.get().upload(file.path)
            .option("public_id", "user_profile_${updatedUser.id}")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    binding.progressBar.visibility = View.VISIBLE
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = (bytes.toDouble() / totalBytes.toDouble() * 100).toInt()
                    binding.progressBar.progress = progress
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    binding.progressBar.visibility = View.GONE
                    val imageUrl = resultData["secure_url"] as? String ?: ""
                    if (imageUrl.isNotEmpty()) {
                        val userWithNewImage = updatedUser.copy(profileImageUrl = imageUrl)
                        viewModel.updateUserProfile(userWithNewImage, bitmap)
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Upload failed: ${error.description}", Toast.LENGTH_LONG).show()
                    viewModel.updateUserProfile(updatedUser, null)
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.e("Cloudinary Upload", "Upload rescheduled: ${error.description}")
                }
            })
            .dispatch()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}