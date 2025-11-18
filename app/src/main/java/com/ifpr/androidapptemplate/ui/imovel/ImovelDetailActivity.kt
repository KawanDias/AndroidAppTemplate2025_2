package com.ifpr.androidapptemplate.ui.imovel

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Imovel
import java.text.NumberFormat
import java.util.Locale

class ImovelDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imovel_detail)

        val imovel = intent.getParcelableExtra<Imovel>("IMOVEL_EXTRA")

        if (imovel != null) {
            val viewPager: ViewPager2 = findViewById(R.id.view_pager_images)
            val txtTitulo: TextView = findViewById(R.id.txt_titulo_detail)
            val txtPreco: TextView = findViewById(R.id.txt_preco_detail)
            val txtModalidade: TextView = findViewById(R.id.txt_modalidade_detail)
            val txtQuartos: TextView = findViewById(R.id.txt_quartos_detail)
            val txtBanheiros: TextView = findViewById(R.id.txt_banheiros_detail)
            val txtArea: TextView = findViewById(R.id.txt_area_detail)
            val txtEndereco: TextView = findViewById(R.id.txt_endereco_detail)
            val btnAddPhoto: Button = findViewById(R.id.btn_add_photo)

            txtTitulo.text = imovel.titulo
            txtModalidade.text = imovel.modalidade
            txtEndereco.text = "${imovel.endereco}, ${imovel.numero}"

            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            txtPreco.text = formatador.format(imovel.preco ?: 0.0)

            txtQuartos.text = "${imovel.quartos} quartos"
            txtBanheiros.text = "${imovel.banheiros} banheiros"
            txtArea.text = "${imovel.metragem} m²"

            val images = imovel.base64Images ?: imovel.imageUrls ?: emptyList()
            val isBase64 = imovel.base64Images != null
            viewPager.adapter = ImageSliderAdapter(images, isBase64)

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && currentUser.uid == imovel.userId) {
                btnAddPhoto.visibility = View.VISIBLE
            } else {
                btnAddPhoto.visibility = View.GONE
            }

            btnAddPhoto.setOnClickListener {
                // Lógica para adicionar mais fotos
            }
        }
    }
}
