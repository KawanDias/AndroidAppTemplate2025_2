package com.ifpr.androidapptemplate.ui.usuario

import android.content.Intent
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Imovel
import com.ifpr.androidapptemplate.ui.imovel.ImovelDetailActivity
import java.text.NumberFormat
import java.util.Locale

class FavoritosAdapter(private val favoritos: MutableList<Imovel>) : RecyclerView.Adapter<FavoritosAdapter.FavoritoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_favorito, parent, false)
        return FavoritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {
        val imovel = favoritos[position]
        holder.bind(imovel)
    }

    override fun getItemCount(): Int = favoritos.size

    inner class FavoritoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtTitulo: TextView = itemView.findViewById(R.id.item_titulo)
        private val txtPreco: TextView = itemView.findViewById(R.id.item_preco)
        private val imageView: ImageView = itemView.findViewById(R.id.item_image)
        private val btnRemover: ImageView = itemView.findViewById(R.id.btn_remover_favorito)

        fun bind(imovel: Imovel) {
            txtTitulo.text = imovel.titulo

            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            txtPreco.text = formatador.format(imovel.preco ?: 0.0)

            // Prioriza imagem Base64, depois URL
            val base64Image = imovel.base64Images?.firstOrNull()
            if (!base64Image.isNullOrEmpty()) {
                try {
                    val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                    Glide.with(itemView.context)
                        .asBitmap()
                        .load(imageBytes)
                        .into(imageView)
                } catch (e: IllegalArgumentException) {
                    imageView.setImageResource(R.drawable.placeholder_image)
                }
            } else {
                val imageUrl = imovel.imageUrls?.firstOrNull()
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.placeholder_image)
                }
            }

            btnRemover.setOnClickListener { removerFavorito(imovel) }

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, ImovelDetailActivity::class.java)
                intent.putExtra("IMOVEL_EXTRA", imovel)
                context.startActivity(intent)
            }
        }

        private fun removerFavorito(imovel: Imovel) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && imovel.key.isNotEmpty()) {
                val databaseRef = FirebaseDatabase.getInstance().getReference("users/${user.uid}/favoritos/${imovel.key}")
                databaseRef.removeValue().addOnSuccessListener {
                    Toast.makeText(itemView.context, "Removido dos favoritos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}