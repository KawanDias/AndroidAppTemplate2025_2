package com.ifpr.androidapptemplate.ui.imovel

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Imovel
import java.text.NumberFormat
import java.util.Locale

class ImovelDetailActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isFavorite = false
    private lateinit var btnAddFavorito: Button
    private var imovel: Imovel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imovel_detail)

        auth = FirebaseAuth.getInstance()
        imovel = intent.getParcelableExtra<Imovel>("IMOVEL_EXTRA")

        if (imovel != null) {
            val viewPager: ViewPager2 = findViewById(R.id.view_pager_images)
            val txtTitulo: TextView = findViewById(R.id.txt_titulo_detail)
            val txtPreco: TextView = findViewById(R.id.txt_preco_detail)
            val txtModalidade: TextView = findViewById(R.id.txt_modalidade_detail)
            val txtQuartos: TextView = findViewById(R.id.txt_quartos_detail)
            val txtBanheiros: TextView = findViewById(R.id.txt_banheiros_detail)
            val txtArea: TextView = findViewById(R.id.txt_area_detail)
            val txtEndereco: TextView = findViewById(R.id.txt_endereco_detail)
            val btnVerMapa: Button = findViewById(R.id.btn_ver_mapa)
            btnAddFavorito = findViewById(R.id.btn_add_favorito)

            txtTitulo.text = imovel!!.titulo
            txtModalidade.text = imovel!!.modalidade
            txtEndereco.text = "${imovel!!.endereco}, ${imovel!!.numero}"

            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            txtPreco.text = formatador.format(imovel!!.preco ?: 0.0)

            txtQuartos.text = "${imovel!!.quartos} quartos"
            txtBanheiros.text = "${imovel!!.banheiros} banheiros"
            txtArea.text = "${imovel!!.metragem} m²"

            val images = imovel!!.base64Images ?: imovel!!.imageUrls ?: emptyList()
            val isBase64 = imovel!!.base64Images != null
            viewPager.adapter = ImageSliderAdapter(images, isBase64)

            btnVerMapa.visibility = View.VISIBLE

            btnVerMapa.setOnClickListener {
                val endereco = imovel!!.endereco
                val numero = imovel!!.numero
                val fullAddress = "$endereco, $numero"

                val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(fullAddress)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(this, "Nenhum app de mapa encontrado.", Toast.LENGTH_SHORT).show()
                }
            }

            checkIfFavorite()

            btnAddFavorito.setOnClickListener {
                toggleFavorite()
            }
        } else {
            Toast.makeText(this, "Erro ao carregar detalhes do imóvel.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkIfFavorite() {
        val currentUser = auth.currentUser
        if (currentUser != null && imovel?.key != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}/favoritos/${imovel!!.key}")
            databaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFavorite = snapshot.exists()
                    updateFavoriteButton()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ImovelDetailActivity, "Erro ao verificar favoritos.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Oculta o botão se o usuário não estiver logado ou o imóvel não tiver uma chave
            btnAddFavorito.visibility = View.GONE
        }
    }

    private fun toggleFavorite() {
        val currentUser = auth.currentUser
        if (currentUser != null && imovel?.key != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}/favoritos/${imovel!!.key}")
            if (isFavorite) {
                databaseRef.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Imóvel removido dos favoritos", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erro ao remover dos favoritos.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Salva o objeto Imovel inteiro nos favoritos
                val imovelRef = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}/favoritos/${imovel!!.key}")
                imovelRef.setValue(imovel).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Imóvel adicionado aos favoritos", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Erro ao adicionar aos favoritos.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateFavoriteButton() {
        if (isFavorite) {
            btnAddFavorito.text = "Remover dos favoritos"
        } else {
            btnAddFavorito.text = "Adicionar aos favoritos"
        }
    }
}
