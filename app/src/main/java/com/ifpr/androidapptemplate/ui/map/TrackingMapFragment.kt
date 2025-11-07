package com.ifpr.androidapptemplate.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.*
import com.google.maps.android.ui.IconGenerator
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Server
import com.ifpr.androidapptemplate.baseclasses.Usuario // Caminho correto para Usuario

class TrackingMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val serversRef = FirebaseDatabase.getInstance().getReference("servers")
    private val usuariosRef = FirebaseDatabase.getInstance().getReference("usuarios")

    private val profileCache = mutableMapOf<String, Usuario>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tracking_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        fetchUserProfiles()
    }

    private fun fetchUserProfiles() {
        usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { userSnapshot ->
                    val user = userSnapshot.getValue<Usuario>()
                    // Se o seu Firebase usa 'uid' como chave (snapshot.key) e na classe 'key' é a variável:
                    user?.key = userSnapshot.key
                    user?.let { profileCache[it.key!!] = it }
                }
                listenForServerLocations()
            }
            override fun onCancelled(error: DatabaseError) { /* Tratar erro */ }
        })
    }

    private fun listenForServerLocations() {
        serversRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                googleMap.clear()
                var firstLocation: LatLng? = null

                snapshot.children.forEach { serverSnapshot ->
                    val server = serverSnapshot.getValue<Server>()
                    val uid = serverSnapshot.key

                    if (server != null && uid != null && server.latitude != 0.0 && server.longitude != 0.0) {
                        val latLng = LatLng(server.latitude, server.longitude)
                        val userProfile = profileCache[uid]

                        // *** CORREÇÃO AQUI ***: Usando 'nomeCompleto' conforme sua classe Usuario
                        val name = userProfile?.nomeCompleto ?: "Professor Desconhecido"
                        val photoUrl = userProfile?.fotoUrl

                        if (!photoUrl.isNullOrEmpty()) {
                            createMarkerWithPhoto(requireContext(), latLng, name, server.status, photoUrl)
                        } else {
                            val markerOptions = MarkerOptions()
                                .position(latLng)
                                .title(name)
                                .snippet("Status: ${server.status}")
                            googleMap.addMarker(markerOptions)?.showInfoWindow()
                        }

                        if (firstLocation == null) {
                            firstLocation = latLng
                        }
                    }
                }

                firstLocation?.let {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                }
            }
            override fun onCancelled(error: DatabaseError) { /* Tratar erro */ }
        })
    }

    private fun createMarkerWithPhoto(context: Context, latLng: LatLng, name: String, status: String, photoUrl: String) {
        // Usamos R.mipmap pois 'ic_launcher_round' geralmente está lá.
        Glide.with(context).asBitmap().load(photoUrl).placeholder(R.mipmap.ic_launcher_round).circleCrop()
            .into(object : SimpleTarget<Bitmap>(100, 100) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val iconGenerator = IconGenerator(context)
                    val imageView = ImageView(context)
                    imageView.setImageBitmap(resource)
                    iconGenerator.setContentView(imageView)
                    val icon = iconGenerator.makeIcon()

                    val markerOptions = MarkerOptions()
                        .position(latLng).title(name).snippet("Status: $status")
                        .icon(BitmapDescriptorFactory.fromBitmap(icon))

                    googleMap.addMarker(markerOptions)?.showInfoWindow()
                }
            })
    }
}