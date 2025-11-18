package com.ifpr.androidapptemplate.ui.imovel

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Imovel
import java.text.NumberFormat
import java.util.Locale

class ImovelAdapter(private val imoveis: List<Imovel>) :
    RecyclerView.Adapter<ImovelAdapter.ImovelViewHolder>() {

    class ImovelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgImovel: ImageView = view.findViewById(R.id.imgImovel)
        val txtModalidade: TextView = view.findViewById(R.id.txtModalidade)
        val txtTitulo: TextView = view.findViewById(R.id.txtTitulo)
        val txtPreco: TextView = view.findViewById(R.id.txtPreco)
        val txtEndereco: TextView = view.findViewById(R.id.txtEndereco)
        val txtDetalhes: TextView = view.findViewById(R.id.txtDetalhes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImovelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_imovel_card, parent, false)
        return ImovelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImovelViewHolder, position: Int) {
        val imovel = imoveis[position]
        holder.txtModalidade.text = imovel.modalidade
        holder.txtTitulo.text = imovel.titulo
        holder.txtEndereco.text = "${imovel.endereco}, ${imovel.numero}"

        val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        holder.txtPreco.text = formatador.format(imovel.preco ?: 0.0)

        holder.txtDetalhes.text = "${imovel.quartos} quartos - ${imovel.banheiros} banheiros - ${imovel.metragem}m²"

        if (!imovel.base64Image.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(imovel.base64Image, Base64.DEFAULT)
                holder.imgImovel.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            } catch (e: Exception) {
                if (!imovel.imageUrl.isNullOrEmpty()) {
                    Glide.with(holder.itemView.context).load(imovel.imageUrl).into(holder.imgImovel)
                } else {
                    holder.imgImovel.setImageResource(R.drawable.placeholder_image) // Imagem padrão
                }
            }
        } else if (!imovel.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context).load(imovel.imageUrl).into(holder.imgImovel)
        } else {
            holder.imgImovel.setImageResource(R.drawable.placeholder_image) // Imagem padrão
        }
    }

    override fun getItemCount(): Int = imoveis.size
}
