package com.ifpr.androidapptemplate.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.databinding.NotificationItemBinding
import com.ifpr.androidapptemplate.model.Notification

class NotificationAdapter(
    private val notifications: List<Notification>,
    private val onItemClick: (Notification) -> Unit, // Listener for item click
    private val onDeleteClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(val binding: NotificationItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NotificationItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.binding.notificationTitle.text = notification.title
        holder.binding.notificationContent.text = notification.content
        holder.binding.notificationPrice.text = notification.price

        // Handle item click
        holder.itemView.setOnClickListener {
            onItemClick(notification)
        }

        holder.binding.btnDeleteNotification.setOnClickListener {
            onDeleteClick(notification)
        }
    }

    override fun getItemCount() = notifications.size
}
