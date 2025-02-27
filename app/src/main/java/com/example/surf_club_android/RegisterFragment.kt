package com.example.surf_club_android

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
import com.example.surf_club_android.databinding.FragmentRegisterBinding
import com.example.surf_club_android.model.Model

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

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
        val roles = arrayOf("מדריך", "מתנדב", "בית הלוחם ת''א", "בית הלוחם ירושלים", "אקי''ם", "הצעד הבא")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRole.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvBackToLogin.setOnClickListener {
            (activity as? AuthActivity)?.navigateToLogin()
        }

        binding.ivProfileImage.setOnClickListener {
            getContent.launch("image/*")
        }
    }

    private fun registerUser() {
        val firstName = binding.etFirstName.text.toString()
        val lastName = binding.etLastName.text.toString()
        val role = binding.actvRole.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if (firstName.isNotBlank() && lastName.isNotBlank() && role.isNotBlank() &&
            email.isNotBlank() && password.isNotBlank()) {

            val fullName = "$firstName $lastName"
            // For this example, we pass the role as the bio.
            // Convert selected image URI to Bitmap if available.
            val bitmap: Bitmap? = selectedImageUri?.let { getBitmapFromUri(it) }

            Model.shared.signUp(email, password, firstName, lastName, role, bitmap) { firebaseUser, error ->
                activity?.runOnUiThread {
                    if (firebaseUser != null) {
                        (activity as? AuthActivity)?.navigateToMainActivity()
                    } else {
                        Toast.makeText(requireContext(), error ?: "Sign up failed", Toast.LENGTH_LONG).show()
                    }
                }
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
