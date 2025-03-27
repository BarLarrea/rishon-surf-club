package com.example.surf_club_android.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.surf_club_android.R
import com.example.surf_club_android.view.adapters.CategoryAdapter
import com.example.surf_club_android.databinding.FragmentParticipantsBinding
import com.example.surf_club_android.viewmodel.ParticipantsViewModel

class ParticipantsFragment : Fragment() {
    private var _binding: FragmentParticipantsBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("View binding is null")
    private lateinit var viewModel: ParticipantsViewModel
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var postId: String

    private val args: ParticipantsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentParticipantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ParticipantsViewModel::class.java]
        postId = args.postId
        viewModel.loadParticipants(postId)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { user ->
            val action = ParticipantsFragmentDirections
                .actionParticipantsFragmentToUserProfileFragment(user.id)
            findNavController().navigate(action)
        }

        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    private fun setupObservers() {
        viewModel.totalParticipants.observe(viewLifecycleOwner) { total ->
            binding.totalParticipantsCount.text = getString(R.string.total_participants, total)
        }

        viewModel.categoryParticipants.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories.entries.toList())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
