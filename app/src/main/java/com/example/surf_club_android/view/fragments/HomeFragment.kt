package com.example.surf_club_android.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.surf_club_android.databinding.FragmentHomeBinding
import com.example.surf_club_android.model.Model
import com.example.surf_club_android.adapter.PostAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadPosts()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()
        binding.recyclerViewPosts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun loadPosts() {
        binding.progressBar.visibility = View.VISIBLE
        Model.shared.getAllPosts { posts ->
            binding.progressBar.visibility = View.GONE
            if (posts.isNotEmpty()) {
                posts.forEach { post ->
                    Log.d("HomeFragment", "Post: ${post.id}, Author: ${post.date}")
                }
                postAdapter.submitList(posts)
            } else {
                Toast.makeText(requireContext(), "No posts available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}