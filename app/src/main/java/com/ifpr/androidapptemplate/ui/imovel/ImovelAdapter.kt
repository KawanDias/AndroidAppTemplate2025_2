package com.ifpr.androidapptemplate.ui.imovel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Imovel

class ImovelAdapter(private val imoveis: List<Imovel>) :
    RecyclerView.Adapter<ImovelAdapter.ImovelViewHolder>() {

    class ImovelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgImovel: ImageView = view.findViewById(R.id.imgImovel)
        val txtTitulo: TextView = view.findViewById(R.id.txtTitulo)
        val txtPreco: TextView = view.findViewById(R.id.txtPreco)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImovelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_imovel_card, parent, false)
        return ImovelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImovelViewHolder, position: Int) {
        val imovel = imoveis[position]
        holder.txtTitulo.text = imovel.titulo
        holder.txtPreco.text = imovel.preco
        holder.imgImovel.setImageResource(imovel.imagemResId)
    }

    override fun getItemCount(): Int = imoveis.size
}
