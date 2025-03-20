package com.example.surf_club_android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.surf_club_android.R
import io.noties.markwon.Markwon
import android.content.Context

class ChatAdapter(
    private val messages: List<Pair<String, Boolean>>,
    private val context: Context
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    // Constants for the view types
    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    // Return the view type based on whether the message is sent or received
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].second) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    // ViewHolder class to hold references to views in the chat bubble layout
    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageTextView: TextView = view.findViewById(R.id.messageTextView)
    }

    // Create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == VIEW_TYPE_SENT) {
            R.layout.item_chat_bubble_sent
        } else {
            R.layout.item_chat_bubble_received
        }
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    // Bind the data to the views
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position].first
        val isUserMessage = messages[position].second

        if (isUserMessage) {
            holder.messageTextView.text = message
        } else {
            // Use Markdown to render Markdown for Gemini's responses
            val markwon = Markwon.create(context)
            markwon.setMarkdown(holder.messageTextView, message)
        }
    }

    override fun getItemCount(): Int = messages.size
}
