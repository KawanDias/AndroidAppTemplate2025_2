package com.ifpr.androidapptemplate.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item // Importe a classe Item

// Renomeada de TrackingMapFragment
class ImovelMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_imovel_map, container, false)

        // Inicializa o mapa
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true // Ativa controles de zoom

        // Move a câmera para uma posição inicial (ex: Foz do Iguaçu, PR)
        val fozDoIguacu = LatLng(-25.5098, -54.5855)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fozDoIguacu, 12f))

        // Carrega os imóveis do Firebase
        fetchImoveisFromFirebase()
    }

    private fun fetchImoveisFromFirebase() {
        // Referência para o nó 'imoveis' no Firebase
        val databaseRef = FirebaseDatabase.getInstance().getReference("imoveis")

        databaseRef.addValueEventListener(object : ValueEventListener { // addValueEventListener para atualizações em tempo real
            override fun onDataChange(snapshot: DataSnapshot) {
                googleMap.clear() // Limpa todos os marcadores existentes antes de adicionar os novos

                for (imovelSnapshot in snapshot.children) {
                    val imovel = imovelSnapshot.getValue(Item::class.java) // Pega o objeto Item

                    if (imovel != null && imovel.latitude != 0.0 && imovel.longitude != 0.0) {
                        val latLng = LatLng(imovel.latitude, imovel.longitude)

                        // Cria o título do marcador com Título e Preço
                        val markerTitle = "${imovel.titulo ?: "Imóvel"} - R$ ${imovel.preco ?: "0.00"}"
                        val markerSnippet = "Tipo: ${imovel.tipo ?: "Não informado"}\nEndereço: ${imovel.endereco ?: "Não informado"}"

                        googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(markerTitle)
                                .snippet(markerSnippet)
                        )
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Erro ao carregar imóveis: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}