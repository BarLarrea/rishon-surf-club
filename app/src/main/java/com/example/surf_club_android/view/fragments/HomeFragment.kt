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
import com.example.surf_club_android.view.fragments.adapters.PostAdapter
import com.example.surf_club_android.databinding.FragmentHomeBinding
import com.example.surf_club_android.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding?: throw IllegalStateException("View binding is null")
    private lateinit var viewModel: HomeViewModel
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        parentFragmentManager.setFragmentResultListener("shouldRefreshHome", viewLifecycleOwner) { _, _ ->
            viewModel.loadPosts()
        }
        parentFragmentManager.setFragmentResultListener("postUpdated", viewLifecycleOwner) { _, _ ->
            viewModel.loadPosts()
        }
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            isProfileView = false,
            onPostRemoved = { deletedPostId ->
                val updatedPosts = viewModel.posts.value?.filter { it.id != deletedPostId } ?: emptyList()
                postAdapter.submitList(updatedPosts)
            },
            onUpdate = { post ->
                val action = HomeFragmentDirections.actionHomeFragmentToUpdatePostFragment(post.id)
                findNavController().navigate(action)
            },
            onParticipantsClick = { post ->
                val action = HomeFragmentDirections.actionHomeFragmentToParticipantsFragment(post.id)
                findNavController().navigate(action)
            }
        )
        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun setupObservers() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}