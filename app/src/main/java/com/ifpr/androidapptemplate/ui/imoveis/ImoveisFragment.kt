package com.ifpr.androidapptemplate.ui.imoveis

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentImoveisBinding
import java.text.NumberFormat
import java.util.Locale

class ImoveisFragment : Fragment() {

    private var _binding: FragmentImoveisBinding? = null
    private val binding get() = _binding!!

    private val allImoveisRef = FirebaseDatabase.getInstance().getReference("imoveis")
    private var imoveisListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImoveisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFirebaseListener()
    }

    private fun setupFirebaseListener() {
        imoveisListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loadAllImoveis(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(context, "Erro ao carregar o catálogo: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        allImoveisRef.addValueEventListener(imoveisListener!!)
    }

    private fun loadAllImoveis(snapshot: DataSnapshot) {
        if (!isAdded) return
        val container = binding.itemContainerImoveis
        container.removeAllViews()

        val imoveisList = mutableListOf<Item>()

        for (userSnapshot in snapshot.children) {
            for (itemSnapshot in userSnapshot.children) {
                try {
                    val item = parseItem(itemSnapshot)
                    if (item != null) {
                        imoveisList.add(item)
                    }
                } catch (e: Exception) {
                    Log.e("ImoveisFragment", "Falha ao processar imóvel: ${itemSnapshot.key}", e)
                }
            }
        }

        if (imoveisList.isEmpty()) {
            val emptyMessage = TextView(context).apply {
                text = "Nenhum imóvel cadastrado."
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(0, 32, 0, 32)
            }
            container.addView(emptyMessage)
        } else {
            imoveisList.forEach { item ->
                addItemView(container, item)
            }
        }
    }
    
    private fun parseItem(snapshot: DataSnapshot): Item? {
        val titulo = snapshot.child("titulo").getValue(String::class.java)
        if (titulo == null) return null // Pula item se não tiver título

        val precoValue = snapshot.child("preco").value
        val precoDouble = when (precoValue) {
            is Double -> precoValue
            is Long -> precoValue.toDouble()
            is String -> precoValue.replace(Regex("[^0-9,.]"), "").replace(",", ".").toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

        return Item(
            key = snapshot.key,
            userId = snapshot.child("userId").getValue(String::class.java),
            titulo = titulo,
            tipo = snapshot.child("tipo").getValue(String::class.java),
            preco = precoDouble,
            quartos = snapshot.child("quartos").getValue(Int::class.java),
            banheiros = snapshot.child("banheiros").getValue(Int::class.java),
            metragem = snapshot.child("metragem").getValue(Double::class.java),
            endereco = snapshot.child("endereco").getValue(String::class.java),
            latitude = snapshot.child("latitude").getValue(Double::class.java) ?: 0.0,
            longitude = snapshot.child("longitude").getValue(Double::class.java) ?: 0.0,
            imageUrl = snapshot.child("imageUrl").getValue(String::class.java),
            base64Image = snapshot.child("base64Image").getValue(String::class.java)
        )
    }

    private fun addItemView(container: LinearLayout, item: Item) {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_imovel_card, container, false)

        val imageView = itemView.findViewById<ImageView>(R.id.imgImovel)
        val tituloView = itemView.findViewById<TextView>(R.id.txtTitulo)
        val precoView = itemView.findViewById<TextView>(R.id.txtPreco)

        tituloView.text = item.titulo ?: "Sem Título"
        formatarPreco(precoView, item.preco)
        carregarImagem(imageView, item)

        container.addView(itemView)
    }

    private fun carregarImagem(imageView: ImageView, item: Item) {
        if (!item.base64Image.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.placeholder_image)
            }
        } else if (!item.imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(item.imageUrl).placeholder(R.drawable.placeholder_image).into(imageView)
        } else {
            imageView.setImageResource(R.drawable.placeholder_image)
        }
    }

    private fun formatarPreco(precoView: TextView, precoValue: Double?) {
        val preco = precoValue ?: 0.0
        val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        precoView.text = formatadorMoeda.format(preco)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imoveisListener?.let {
            allImoveisRef.removeEventListener(it)
        }
        _binding = null
    }
}
