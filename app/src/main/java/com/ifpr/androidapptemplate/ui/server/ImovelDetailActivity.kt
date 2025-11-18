/*
package com.ifpr.androidapptemplate.ui.server

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Imovel
import java.text.NumberFormat
import java.util.*

class ImovelDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imovel_detail)

        val imovel = intent.getParcelableExtra<Imovel>("IMOVEL_EXTRA")

        imovel?.let {
            setupViews(it)
        }
    }

    private fun setupViews(imovel: Imovel) {
        val imgImovel: ImageView = findViewById(R.id.img_imovel_detail)
        val tvTitulo: TextView = findViewById(R.id.tv_imovel_titulo)
        val tvPreco: TextView = findViewById(R.id.tv_imovel_preco)
        val tvModalidade: TextView = findViewById(R.id.tv_imovel_modalidade)
        val tvTipo: TextView = findViewById(R.id.tv_imovel_tipo)
        val tvEndereco: TextView = findViewById(R.id.tv_imovel_endereco)
        val tvQuartos: TextView = findViewById(R.id.tv_imovel_quartos)
        val tvBanheiros: TextView = findViewById(R.id.tv_imovel_banheiros)
        val tvMetragem: TextView = findViewById(R.id.tv_imovel_metragem)

        tvTitulo.text = imovel.titulo

        val formatador = NumberFormat.getInstance(Locale("pt", "BR"))
        formatador.minimumFractionDigits = 2
        tvPreco.text = formatador.format(imovel.preco ?: 0.0)

        tvModalidade.text = "Modalidade: ${imovel.modalidade}"
        tvTipo.text = "Tipo: ${imovel.tipo}"
        tvEndereco.text = "Endereço: ${imovel.endereco}, ${imovel.numero}"
        tvQuartos.text = "Quartos: ${imovel.quartos}"
        tvBanheiros.text = "Banheiros: ${imovel.banheiros}"
        tvMetragem.text = "Metragem: ${imovel.metragem} m²"

        val imageUrl = imovel.imageUrls?.firstOrNull()
        if (imageUrl != null) {
            Glide.with(this).load(imageUrl).into(imgImovel)
        } else {
            imgImovel.setImageResource(R.drawable.placeholder_image)
        }
    }
}
*/