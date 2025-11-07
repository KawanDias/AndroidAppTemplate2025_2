package com.ifpr.androidapptemplate.ui.home

import android.content.ActivityNotFoundException
import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.util.Base64
import android.widget.*
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding
import com.ifpr.androidapptemplate.ui.ai.AiLogicActivity
import com.ifpr.androidapptemplate.ui.server.ServerManagementActivity // NOVO IMPORT: CRUD Activity
// IMPORT NOVO: Necessário para a navegação com o Navigation Component
import androidx.navigation.fragment.findNavController


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private var lastKnownLocation: Location? = null
    private lateinit var currentAddressTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private lateinit var  btnOpenMaps: Button
    private lateinit var  btnOpenWaze: Button
    private lateinit var btnManageServers: Button

    // NOVO: Declaração do botão de rastreamento de professores
    private lateinit var btnOpenTrackingMap: Button

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        inicializaGerenciamentoLocalizacao(view)

        val container = view.findViewById<LinearLayout>(R.id.itemContainer)
        carregarItensMarketplace(container)

        // Botão para abrir o Google Maps
        btnOpenMaps = view.findViewById<Button>(R.id.btnOpenMaps)
        btnOpenMaps.setOnClickListener {
            openInGoogleMaps()
        }

        // Botão para abrir o Waze
        btnOpenWaze = view.findViewById<Button>(R.id.btnOpenWaze)
        btnOpenWaze.setOnClickListener {
            openInWaze()
        }

        // BOTÃO PARA GERENCIAR PROFESSORES (CRUD)
        btnManageServers = view.findViewById<Button>(R.id.btnManageServers)
        btnManageServers.setOnClickListener {
            val context = view.context
            val intent = Intent(context, ServerManagementActivity::class.java)
            context.startActivity(intent)
        }

        // NOVO CÓDIGO AQUI: Conexão do botão RASTREAR PROFESSORES
        // 1. Assumindo que o ID do botão é `btnOpenTrackingMap`
        btnOpenTrackingMap = view.findViewById<Button>(R.id.btnOpenTrackingMap)
        btnOpenTrackingMap.setOnClickListener {
            // 2. Chama a AÇÃO definida no mobile_navigation.xml
            findNavController().navigate(R.id.action_navigation_home_to_trackingMapFragment)
        }
        // FIM DO NOVO CÓDIGO


        // Botão flutuante de IA (do professor)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_ai)

        fab.setOnClickListener {
            val context = view.context
            val intent = Intent(context, AiLogicActivity::class.java)
            context.startActivity(intent)
        }

        return view
    }


    private fun inicializaGerenciamentoLocalizacao(view: View) {
        currentAddressTextView = view.findViewById(R.id.currentAddressTextView)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            getCurrentLocation()
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Snackbar.make(
                    requireView(),
                    "Permission denied. Cannot access location.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    displayAddress(location)
                }
            }
        }

        locationRequest = LocationRequest.create().apply {
            interval = 30000 // Intervalo em milissegundos para atualizacoes de localizacao
            fastestInterval =
                30000 // O menor intervalo de tempo para receber atualizacoes de localizacao
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun displayAddress(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        lastKnownLocation = location

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
                withContext(Dispatchers.Main) {
                    currentAddressTextView.text = address
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    currentAddressTextView.text = "Error: ${e.message}"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Removendo a atualização de localização quando o Fragment é destruído
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        _binding = null
    }

    fun carregarItensMarketplace(container: LinearLayout) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("itens")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews()

                for (userSnapshot in snapshot.children) {
                    for (itemSnapshot in userSnapshot.children) {
                        val item = itemSnapshot.getValue(Item::class.java) ?: continue

                        val itemView = LayoutInflater.from(container.context)
                            .inflate(R.layout.item_template, container, false)

                        val imageView = itemView.findViewById<ImageView>(R.id.item_image)
                        val enderecoView = itemView.findViewById<TextView>(R.id.item_endereco)

                        enderecoView.text = "Endereço: ${item.endereco ?: "Não informado"}"

                        if (!item.imageUrl.isNullOrEmpty()) {
                            Glide.with(container.context).load(item.imageUrl).into(imageView)
                        } else if (!item.base64Image.isNullOrEmpty()) {
                            try {
                                val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                imageView.setImageBitmap(bitmap)
                            } catch (_: Exception) {}
                        }

                        container.addView(itemView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(container.context, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openInGoogleMaps() {
        val location = lastKnownLocation
        if (location == null) {
            Toast.makeText(context, "Localização não disponível. Aguarde o GPS.", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(Minha Localização)")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Se o Google Maps não estiver instalado, abre Play Store
            val playStoreUri = Uri.parse("market://details?id=com.google.android.apps.maps")
            startActivity(Intent(Intent.ACTION_VIEW, playStoreUri))
        }
    }


    private fun openInWaze() {
        val location = lastKnownLocation
        if (location == null) {
            Toast.makeText(context, "Localização não disponível. Aguarde o GPS.", Toast.LENGTH_SHORT).show()
            return
        }

        val latitude = location.latitude
        val longitude = location.longitude

        // 1. Tenta abrir no WAZE
        try {
            val wazeUri = Uri.parse("waze://?ll=${latitude},${longitude}&navigate=yes")
            val wazeIntent = Intent(Intent.ACTION_VIEW, wazeUri)
            startActivity(wazeIntent)
        } catch (e: ActivityNotFoundException) {
            // 2. Se o WAZE falhar, tenta o Google Maps (fallback mais robusto)
            try {
                val mapsUri = Uri.parse("geo:${latitude},${longitude}?q=${latitude},${longitude}(Destino)")
                val mapsIntent = Intent(Intent.ACTION_VIEW, mapsUri).apply {
                    setPackage("com.google.android.apps.maps") // Garante que Maps seja o alvo
                }
                startActivity(mapsIntent)
            } catch (e: ActivityNotFoundException) {
                // 3. Se nenhum dos dois funcionar, mostra o erro final
                Toast.makeText(context, "Nenhum aplicativo de navegação (Waze ou Maps) encontrado.", Toast.LENGTH_LONG).show()
            }
        }
    }


}