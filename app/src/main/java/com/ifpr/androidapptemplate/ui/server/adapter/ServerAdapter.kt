// Caminho: com.ifpr.androidapptemplate.ui.server.adapter.ServerAdapter.kt

package com.ifpr.androidapptemplate.ui.server.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.databinding.ItemServerBinding
import com.ifpr.androidapptemplate.model.Server

class ServerAdapter(
    private val serverList: List<Server>,
    private val onEdit: (Server) -> Unit,
    private val onDelete: (Server) -> Unit
) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val binding = ItemServerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        holder.bind(serverList[position])
    }

    override fun getItemCount(): Int = serverList.size

    inner class ServerViewHolder(private val binding: ItemServerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(server: Server) {
            binding.tvServerName.text = server.name

            binding.btnEdit.setOnClickListener {
                onEdit(server)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(server)
            }
        }
    }
}