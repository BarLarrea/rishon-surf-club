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
import com.example.surf_club_android.R
import com.example.surf_club_android.adapter.PostAdapter
import com.example.surf_club_android.databinding.FragmentHomeBinding
import com.example.surf_club_android.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            isProfileView = false,
            onPostRemoved = { deletedPostId ->
                // Remove the deleted post from the current list and update the adapter.
                val updatedPosts = viewModel.posts.value?.filter { it.id != deletedPostId } ?: emptyList()
                postAdapter.submitList(updatedPosts)
            },
            onUpdate = { post ->
                val bundle = Bundle().apply {
                    putString("postId", post.id)
                }
                // Navigate to your update screen.
                // Ensure your navigation graph defines a destination with the id "updatePostFragment".
                findNavController().navigate(R.id.updatePostFragment, bundle)
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
