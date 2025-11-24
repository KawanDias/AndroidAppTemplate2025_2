package com.ifpr.androidapptemplate.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.databinding.ItemImovelBinding

class ImovelAdapter(private var imoveis: List<Imovel>) : RecyclerView.Adapter<ImovelAdapter.ImovelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImovelViewHolder {
        val binding = ItemImovelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImovelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImovelViewHolder, position: Int) {
        holder.bind(imoveis[position])
    }

    override fun getItemCount() = imoveis.size

    fun updateList(newImoveis: List<Imovel>) {
        imoveis = newImoveis
        notifyDataSetChanged()
    }

    inner class ImovelViewHolder(private val binding: ItemImovelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imovel: Imovel) {
            binding.textViewTitulo.text = imovel.titulo
            binding.textViewPreco.text = imovel.preco
        }
    }
}