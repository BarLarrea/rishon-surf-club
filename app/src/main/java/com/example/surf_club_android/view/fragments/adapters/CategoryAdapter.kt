package com.example.surf_club_android.view.fragments.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.surf_club_android.databinding.ItemCategoryBinding
import com.example.surf_club_android.model.schemas.User

class CategoryAdapter : ListAdapter<Map.Entry<String, List<User>>, CategoryAdapter.CategoryViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val (category, participants) = getItem(position)
        holder.bind(category, participants)
    }

    class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(category: String, participants: List<User>) {
            binding.categoryTitle.text = "$category (${participants.size})"

            val adapter = ParticipantAdapter()
            binding.participantsRecyclerView.layoutManager =
                LinearLayoutManager(binding.root.context)
            binding.participantsRecyclerView.adapter = adapter
            adapter.submitList(participants)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Map.Entry<String, List<User>>>() {
        override fun areItemsTheSame(oldItem: Map.Entry<String, List<User>>, newItem: Map.Entry<String, List<User>>) =
            oldItem.key == newItem.key

        override fun areContentsTheSame(oldItem: Map.Entry<String, List<User>>, newItem: Map.Entry<String, List<User>>) =
            oldItem.value.size == newItem.value.size && oldItem.value.containsAll(newItem.value)
    }
}