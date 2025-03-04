package com.example.surf_club_android.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.surf_club_android.R
import com.example.surf_club_android.adapter.PostAdapter
import com.example.surf_club_android.databinding.FragmentUserProfileBinding
import com.example.surf_club_android.viewmodel.UserProfileViewModel
import com.google.firebase.auth.FirebaseAuth

class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: UserProfileViewModel
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]

        FirebaseAuth.getInstance().currentUser?.let { firebaseUser ->
            viewModel.loadUser(firebaseUser.uid)
            viewModel.loadUserSessions(firebaseUser.uid)
        }

        setupObservers()
        setupRecyclerView()
        setupEditProfileButton()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            isProfileView = true,
            onPostRemoved = { postId ->
                viewModel.removeSession(postId)
            }
        )

        binding.sessionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }



    private fun setupEditProfileButton() {
        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.editUserProfileFragment)
        }
    }

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.nameTextView.text = getString(R.string.full_name_format, it.firstName, it.lastName)
                binding.emailValueTextView.text = it.email
                binding.teamValueTextView.text = it.role
                binding.aboutMeValueTextView.text = it.aboutMe ?: getString(R.string.no_info_provided)

                Glide.with(this)
                    .load(it.profileImageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(binding.profileImage)

                viewModel.loadUserSessions(it.id)
            }
        }

        viewModel.userSessions.observe(viewLifecycleOwner) { sessions ->
            if (sessions.isNotEmpty()) {
                postAdapter.submitList(sessions)
                binding.sessionsRecyclerView.visibility = View.VISIBLE
                binding.noSessionsTextView.visibility = View.GONE
            } else {
                binding.sessionsRecyclerView.visibility = View.GONE
                binding.noSessionsTextView.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
