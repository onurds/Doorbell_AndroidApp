package com.onurds.doorbell

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter

class NotificationLogAdapter : ListAdapter<NotificationLogEntity, NotificationLogAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val timestampText: TextView = view.findViewById(R.id.timestampText)
        private val messageText: TextView = view.findViewById(R.id.messageText)
        private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        fun bind(log: NotificationLogEntity) {
            timestampText.text = log.timestamp.format(formatter)
            messageText.text = log.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.log_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiffCallback : DiffUtil.ItemCallback<NotificationLogEntity>() {
        override fun areItemsTheSame(oldItem: NotificationLogEntity, newItem: NotificationLogEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NotificationLogEntity, newItem: NotificationLogEntity) =
            oldItem == newItem
    }
}
