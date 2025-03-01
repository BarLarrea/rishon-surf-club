package com.example.surf_club_android.view.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.surf_club_android.R
import com.example.surf_club_android.adapter.ParticipantAdapter

class ParticipantListFragment : Fragment(R.layout.fragment_participant_list) {

    private lateinit var adapter: ParticipantAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ParticipantAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.participantsRecyclerView)
        recyclerView.adapter = adapter
    }
}