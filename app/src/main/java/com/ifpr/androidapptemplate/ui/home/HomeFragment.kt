package com.ifpr.androidapptemplate.ui.home

import androidx.navigation.fragment.findNavController
import android.content.ActivityNotFoundException
import android.Manifest
import android.content.pm.PackageManager
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
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.*
import java.util.Locale
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding
import com.ifpr.androidapptemplate.ui.ai.AiLogicActivity
import com.ifpr.androidapptemplate.ui.server.ImovelManagementActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var lastKnownLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private lateinit var currentAddressTextView: TextView
    private lateinit var btnOpenMaps: Button
    private lateinit var btnOpenWaze: Button
    private lateinit var btnManageServers: Button
    private lateinit var btnOpenTrackingMap: Button

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        inicializaGerenciamentoLocalizacao(view)

        val containerItens = view.findViewById<LinearLayout>(R.id.itemContainer)
        // MUDANÇA AQUI: Agora carrega os imóveis marcados como DESTAQUES
        carregarImoveisDestaques(containerItens)

        // Botão abrir Google Maps
        btnOpenMaps = view.findViewById(R.id.btnOpenMaps)
        btnOpenMaps.setOnClickListener { openInGoogleMaps() }

        // Botão abrir Waze
        btnOpenWaze = view.findViewById(R.id.btnOpenWaze)
        btnOpenWaze.setOnClickListener { openInWaze() }

        // Botão Gerenciar Imóveis
        btnManageServers = view.findViewById(R.id.btnManageServers)
        btnManageServers.setOnClickListener {
            val context = view.context
            val intent = Intent(context, ImovelManagementActivity::class.java)
            context.startActivity(intent)
        }

        // Botão Ver mapa de imóveis
        btnOpenTrackingMap = view.findViewById(R.id.btnOpenTrackingMap)
        btnOpenTrackingMap.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_imovelMapFragment)
        }

        // Botão flutuante IA
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_ai)
        fab.setOnClickListener {
            val intent = Intent(view.context, AiLogicActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    // FUNÇÃO ATUALIZADA PARA CARREGAR DO NÓ "destaques"
    private fun carregarImoveisDestaques(container: LinearLayout) {
        // Não precisamos de userId aqui, pois o nó /destaques é público
        val databaseRef = FirebaseDatabase.getInstance().getReference("destaques")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews()

                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(Item::class.java) ?: continue

                    // Adicionamos o key (ID) ao objeto Item para referência futura
                    item.key = itemSnapshot.key

                    val itemView = LayoutInflater.from(container.context)
                        .inflate(R.layout.item_template, container, false)

                    val imageView = itemView.findViewById<ImageView>(R.id.item_image)
                    val enderecoView = itemView.findViewById<TextView>(R.id.item_endereco)
                    val precoView = itemView.findViewById<TextView>(R.id.item_preco)

                    // Usando o campo 'titulo' ou 'endereco'
                    enderecoView.text = item.titulo ?: item.endereco ?: "Sem Título/Endereço"
                    precoView.text = "R$ ${String.format("%.2f", item.preco)}"

                    // Exibe imagem corretamente
                    if (!item.base64Image.isNullOrEmpty()) {
                        try {
                            val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            imageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            // Se falhar o Base64, tenta o imageUrl (se existir)
                            if (!item.imageUrl.isNullOrEmpty()) {
                                Glide.with(requireContext()).load(item.imageUrl).into(imageView)
                            } else {
                                imageView.setImageResource(R.drawable.placeholder_image)
                            }
                        }
                    } else if (!item.imageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext()).load(item.imageUrl).into(imageView)
                    } else {
                        imageView.setImageResource(R.drawable.placeholder_image)
                    }

                    container.addView(itemView)
                }

                if (snapshot.childrenCount == 0L) {
                    val vazio = TextView(container.context)
                    vazio.text = "Nenhum imóvel em destaque."
                    vazio.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    vazio.setPadding(0, 16, 0, 16)
                    container.addView(vazio)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(container.context, "Erro ao carregar destaques: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Métodos de localização e Waze/Maps permanecem inalterados
    private fun inicializaGerenciamentoLocalizacao(view: View) {
        currentAddressTextView = view.findViewById(R.id.currentAddressTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
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
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            Snackbar.make(
                requireView(),
                "Permissão negada. Não é possível acessar a localização.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
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
            interval = 30000
            fastestInterval = 30000
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
                val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Endereço não encontrado"
                withContext(Dispatchers.Main) {
                    currentAddressTextView.text = address
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    currentAddressTextView.text = "Erro: ${e.message}"
                }
            }
        }
    }

    private fun openInGoogleMaps() {
        val location = lastKnownLocation ?: run {
            Toast.makeText(context, "Aguardando GPS...", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(Minha Localização)")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps")))
        }
    }

    private fun openInWaze() {
        val location = lastKnownLocation ?: run {
            Toast.makeText(context, "Aguardando GPS...", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val wazeUri = Uri.parse("waze://?ll=${location.latitude},${location.longitude}&navigate=yes")
            startActivity(Intent(Intent.ACTION_VIEW, wazeUri))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Waze não encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        _binding = null
    }
}