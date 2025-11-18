package com.ifpr.androidapptemplate.ui.imovel

import android.content.Intent
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

        val firstBase64 = imovel.base64Images?.firstOrNull()
        val firstUrl = imovel.imageUrls?.firstOrNull()

        if (firstBase64 != null) {
            try {
                val bytes = Base64.decode(firstBase64, Base64.DEFAULT)
                holder.imgImovel.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            } catch (e: Exception) {
                if (firstUrl != null) {
                    Glide.with(holder.itemView.context).load(firstUrl).into(holder.imgImovel)
                } else {
                    holder.imgImovel.setImageResource(R.drawable.placeholder_image) // Imagem padrão
                }
            }
        } else if (firstUrl != null) {
            Glide.with(holder.itemView.context).load(firstUrl).into(holder.imgImovel)
        } else {
            holder.imgImovel.setImageResource(R.drawable.placeholder_image) // Imagem padrão
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ImovelDetailActivity::class.java)
            intent.putExtra("IMOVEL_EXTRA", imovel)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = imoveis.size
}
