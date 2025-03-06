package com.example.surf_club_android.view.fragments

import com.example.surf_club_android.adapter.ChatAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.surf_club_android.R
import com.example.surf_club_android.databinding.FragmentChatWithKellyBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.surf_club_android.network.GeminiService
import com.example.surf_club_android.viewmodel.ChatViewModel
import com.example.surf_club_android.viewmodel.ChatViewModelFactory
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {

    private var _binding: FragmentChatWithKellyBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter
    private val messagesList = mutableListOf<Pair<String, Boolean>>()

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(GeminiService())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatWithKellyBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatAdapter = ChatAdapter(messagesList)

        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }

        viewModel.response.observe(viewLifecycleOwner) { response ->
            messagesList.add(Pair(response, false)) // Adding Gemini's response
            chatAdapter.notifyItemInserted(messagesList.size - 1)
            binding.recyclerViewChat.scrollToPosition(messagesList.size - 1)
        }

        binding.messageInputArea.findViewById<View>(R.id.sendButton).setOnClickListener {
            val chatInput = binding.messageInputArea.findViewById<android.widget.EditText>(R.id.chatInput)
            val userMessage = chatInput.text.toString()

            if (userMessage.isNotEmpty()) {
                messagesList.add(Pair(userMessage, true)) // ✅ Add user message
                chatAdapter.notifyItemInserted(messagesList.size - 1)
                binding.recyclerViewChat.scrollToPosition(messagesList.size - 1)

                viewModel.sendMessage(userMessage) // ✅ Send message to Gemini

                chatInput.text.clear() // ✅ Clear the input field after sending
            } else {
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
