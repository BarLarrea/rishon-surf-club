package com.example.surf_club_android.view.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.surf_club_android.R
import com.example.surf_club_android.databinding.FragmentRegisterBinding
import com.example.surf_club_android.model.repositories.UserRepository

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("View binding is null")
    private var selectedImageUri: Uri? = null

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.ivProfileImage.setImageURI(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRoleDropdown()
        setupClickListeners()
    }

    private fun setupRoleDropdown() {
        val roles =
            arrayOf("מדריך", "מתנדב", "בית הלוחם ת''א", "בית הלוחם ירושלים", "אקי''ם", "הצעד הבא")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRole.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvBackToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.ivProfileImage.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun uploadImageToCloudinary(bitmap: Bitmap, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        UserRepository.shared.uploadImageToCloudinary(
            image = bitmap,
            name = System.currentTimeMillis().toString(),
            onSuccess = onSuccess,
            onError = onError
        )
    }



    private fun registerUser() {
        val firstName = binding.etFirstName.text.toString()
        val lastName = binding.etLastName.text.toString()
        val role = binding.actvRole.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if (firstName.isNotBlank() && lastName.isNotBlank() && role.isNotBlank() &&
            email.isNotBlank() && password.isNotBlank()) {

            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false

            val bitmap = selectedImageUri?.let { getBitmapFromUri(it) }

            fun continueSignUp(imageBitmap: Bitmap?) {
                UserRepository.shared.signUp(email, password, firstName, lastName, role, imageBitmap) { firebaseUser, error ->
                    activity?.runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        binding.btnRegister.isEnabled = true

                        if (firebaseUser != null) {
                            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                        } else {
                            Toast.makeText(requireContext(), error ?: "Sign up failed", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            if (bitmap != null) {
                uploadImageToCloudinary(bitmap,
                    onSuccess = {
                        continueSignUp(bitmap)
                    },
                    onError = { error ->
                        Toast.makeText(requireContext(), "Image upload failed: $error", Toast.LENGTH_SHORT).show()
                        continueSignUp(null)
                    }
                )
            } else {
                continueSignUp(null)
            }

        } else {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to convert a Uri to a Bitmap.
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