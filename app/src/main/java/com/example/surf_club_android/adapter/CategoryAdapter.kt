package com.example.surf_club_android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surf_club_android.R
import com.example.surf_club_android.databinding.ItemCategorySummaryBinding
import com.example.surf_club_android.model.Category

class CategoryAdapter(private val categories: List<Category>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemCategorySummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.categoryName.text = category.name
            binding.participantCount.text = binding.root.context.getString(
                R.string.participant_count,
                category.participants.size
            )

            val participantAdapter = ParticipantAdapter()
            binding.participantsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = participantAdapter
            }
            participantAdapter.updateParticipants(category.participants)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategorySummaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size
}