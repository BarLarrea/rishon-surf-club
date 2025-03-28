package com.example.surf_club_android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.surf_club_android.model.schemas.Post
import com.example.surf_club_android.model.schemas.User
import com.google.firebase.firestore.FirebaseFirestore

class ParticipantsViewModel : ViewModel() {
    private val _participants = MutableLiveData<List<User>>()
    val participants: LiveData<List<User>> = _participants

    private val _categoryParticipants = MutableLiveData<Map<String, List<User>>>()
    val categoryParticipants: LiveData<Map<String, List<User>>> = _categoryParticipants

    private val _totalParticipants = MutableLiveData<Int>()
    val totalParticipants: LiveData<Int> = _totalParticipants

    fun loadParticipants(postId: String) {
        FirebaseFirestore.getInstance().collection("posts").document(postId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val participantIds = snapshot.toObject(Post::class.java)?.participants ?: emptyList()
                if (participantIds.isEmpty()) {
                    _participants.postValue(emptyList())
                    _categoryParticipants.postValue(emptyMap())
                    _totalParticipants.postValue(0)
                    return@addSnapshotListener
                }

                FirebaseFirestore.getInstance().collection("users")
                    .whereIn("id", participantIds)
                    .get()
                    .addOnSuccessListener { result ->
                        val participantsList = result.toObjects(User::class.java)
                        _participants.postValue(participantsList)

                        // Group participants by role
                        val categorizedMap = participantsList.groupBy { it.role }
                        _categoryParticipants.postValue(categorizedMap)

                        _totalParticipants.postValue(participantsList.size)
                    }
            }
    }
}
