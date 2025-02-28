package com.example.surf_club_android.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.surf_club_android.databinding.FragmentHomeBinding
import com.example.surf_club_android.model.Post
import com.example.surf_club_android.adapter.PostAdapter
import java.util.UUID

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
        val dummyPosts = listOf(
            Post(
                id = UUID.randomUUID().toString(),
                authorName = "Tamar Zohar",
                authorImage = "https://example.com/profile1.jpg",
                date = "8.1.2025",
                waveHeight = "40-50 cm waves",
                windSpeed = "10-15 km/h",
                description = "Perfect day for all levels of surfing\nHope to see you in the water",
                postImage = "https://example.com/surf1.jpg"
            ),
            Post(
                id = UUID.randomUUID().toString(),
                authorName = "John Doe",
                authorImage = "https://example.com/profile2.jpg",
                date = "10.1.2025",
                waveHeight = "60-70 cm waves",
                windSpeed = "5-10 km/h",
                description = "Big waves today! Experienced surfers only.",
                postImage = "https://example.com/surf2.jpg"
            )
        )
        postAdapter.submitList(dummyPosts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}