package com.example.surf_club_android.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.surf_club_android.R
import com.example.surf_club_android.databinding.FragmentEditUserProfileBinding
import com.example.surf_club_android.model.User
import com.example.surf_club_android.viewmodel.EditUserProfileViewModel

class EditUserProfileFragment : Fragment() {

    private var _binding: FragmentEditUserProfileBinding? = null
    private val binding: FragmentEditUserProfileBinding
        get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreateView and onDestroyView")

    private val viewModel: EditUserProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUserProfileBinding.inflate(inflater, container, false)
        return _binding?.root ?: throw IllegalStateException("Binding cannot be null")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
        viewModel.loadUserData()
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

    private fun setupListeners() {
        binding.btnEdit.setOnClickListener {
            val updatedUser = User(
                id = viewModel.user.value?.id.orEmpty(),
                firstName = binding.etFirstName.text.toString(),
                lastName = binding.etLastName.text.toString(),
                email = binding.etEmail.text.toString(),
                role = binding.etRole.text.toString(),
                aboutMe = binding.etAboutMe.text.toString(),
                profileImageUrl = viewModel.user.value?.profileImageUrl.orEmpty()
            )
            viewModel.updateUserProfile(updatedUser)
        }

        binding.tvEditPhoto.setOnClickListener {
            // TODO: Implement photo change functionality
            Toast.makeText(context, R.string.photo_change_not_implemented, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(user: User) {
        binding.etFirstName.setText(user.firstName)
        binding.etLastName.setText(user.lastName)
        binding.etEmail.setText(user.email)
        binding.etRole.setText(user.role)
        binding.etAboutMe.setText(user.aboutMe)

        Glide.with(this)
            .load(user.profileImageUrl)
            .placeholder(R.drawable.ic_profile_placeholder)
            .into(binding.ivProfile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}