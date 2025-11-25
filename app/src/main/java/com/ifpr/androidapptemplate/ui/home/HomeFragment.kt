package com.ifpr.androidapptemplate.ui.home

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.model.Imovel
import com.ifpr.androidapptemplate.ui.ai.AiLogicActivity
import com.ifpr.androidapptemplate.ui.imovel.ImovelDetailActivity
import com.ifpr.androidapptemplate.ui.server.ImovelManagementActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class HomeFragment : Fragment() {

    private var lastKnownLocation: Location? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private lateinit var currentAddressTextView: TextView
    private lateinit var btnOpenMaps: Button
    private lateinit var btnOpenWaze: Button
    private lateinit var btnManageServers: Button

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
        carregarImoveisDestaques(containerItens)

        btnOpenMaps = view.findViewById(R.id.btnOpenMaps)
        btnOpenMaps.setOnClickListener { openInGoogleMaps() }

        btnOpenWaze = view.findViewById(R.id.btnOpenWaze)
        btnOpenWaze.setOnClickListener { openInWaze() }

        btnManageServers = view.findViewById(R.id.btnManageServers)
        btnManageServers.setOnClickListener {
            val intent = Intent(requireContext(), ImovelManagementActivity::class.java)
            startActivity(intent)
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.fab_ai)
        fab.setOnClickListener {
            val intent = Intent(requireContext(), AiLogicActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun carregarImoveisDestaques(container: LinearLayout) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("destaques").orderByChild("timestamp").limitToLast(3)

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                container.removeAllViews()

                val imoveis = mutableListOf<Imovel>()
                for (itemSnapshot in snapshot.children) {
                    val imovel = itemSnapshot.getValue(Imovel::class.java)
                    imovel?.key = itemSnapshot.key ?: ""
                    if (imovel != null) {
                        imoveis.add(imovel)
                    }
                }
                imoveis.reverse() // Exibir os mais recentes primeiro

                if (imoveis.isNotEmpty()) {
                    for (imovel in imoveis) {
                        val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_template, container, false)

                        val imageView = itemView.findViewById<ImageView>(R.id.item_image)
                        val enderecoView = itemView.findViewById<TextView>(R.id.item_endereco)
                        val precoView = itemView.findViewById<TextView>(R.id.item_preco)

                        enderecoView.text = imovel.titulo
                        formatarPreco(precoView, imovel.preco)
                        carregarImagem(imageView, imovel)

                        itemView.setOnClickListener {
                            val intent = Intent(requireContext(), ImovelDetailActivity::class.java)
                            intent.putExtra("IMOVEL_EXTRA", imovel)
                            startActivity(intent)
                        }


                        // Oculta os botões de ação
                        val actionsContainer = itemView.findViewById<LinearLayout>(R.id.item_actions_container)
                        actionsContainer.visibility = View.GONE

                        container.addView(itemView)
                    }
                } else {
                    val vazio = TextView(requireContext())
                    vazio.text = "Nenhum imóvel em destaque."
                    vazio.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    vazio.setPadding(0, 16, 0, 16)
                    container.addView(vazio)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Erro ao carregar destaques: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun formatarPreco(precoView: TextView, precoValue: Double?) {
        val preco = precoValue ?: 0.0
        val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        if (preco == preco.toLong().toDouble()) {
            formatadorMoeda.maximumFractionDigits = 0
        } else {
            formatadorMoeda.maximumFractionDigits = 2
        }
        precoView.text = formatadorMoeda.format(preco)
    }

    private fun carregarImagem(imageView: ImageView, imovel: Imovel) {
        if (!isAdded) return
        val firstBase64 = imovel.base64Images.firstOrNull()
        val firstUrl = imovel.imageUrls.firstOrNull()

        if (firstBase64 != null) {
            try {
                val bytes = Base64.decode(firstBase64, Base64.DEFAULT)
                imageView.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            } catch (e: Exception) {
                if (isAdded && firstUrl != null) {
                    Glide.with(requireContext()).load(firstUrl).into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.placeholder_image)
                }
            }
        } else if (firstUrl != null) {
            if (isAdded) Glide.with(requireContext()).load(firstUrl).into(imageView)
        } else {
            imageView.setImageResource(R.drawable.placeholder_image)
        }
    }

    private fun inicializaGerenciamentoLocalizacao(view: View) {
        currentAddressTextView = view.findViewById(R.id.currentAddressTextView)
        if (!isAdded) return
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
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            if (isAdded) Snackbar.make(requireView(), "Permissão negada.", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun getCurrentLocation() {
        if (!isAdded || (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    if (isAdded) displayAddress(location)
                }
            }
        }

        locationRequest = LocationRequest.create().apply {
            interval = 30000
            fastestInterval = 30000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun displayAddress(location: Location) {
        if (!isAdded) return
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            lastKnownLocation = location
            val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Endereço não encontrado"
            CoroutineScope(Dispatchers.Main).launch {
                currentAddressTextView.text = address
            }
        } catch (e: Exception) {
            CoroutineScope(Dispatchers.Main).launch {
                currentAddressTextView.text = "Erro ao buscar endereço"
            }
        }
    }

    private fun openInGoogleMaps() {
        val location = lastKnownLocation ?: return
        val uri = Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(Minha Localização)")
        val intent = Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps")))
        }
    }

    private fun openInWaze() {
        val location = lastKnownLocation ?: return
        try {
            val wazeUri = Uri.parse("waze://?ll=${location.latitude},${location.longitude}&navigate=yes")
            startActivity(Intent(Intent.ACTION_VIEW, wazeUri))
        } catch (e: ActivityNotFoundException) {
            if (isAdded) Toast.makeText(requireContext(), "Waze não encontrado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}
