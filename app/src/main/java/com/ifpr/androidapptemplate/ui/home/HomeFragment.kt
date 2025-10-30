package com.ifpr.androidapptemplate.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ifpr.androidapptemplate.R // Certifique-se de que esta importação está correta
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Variáveis de Localização
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        binding.btnOpenMaps.setOnClickListener {
            openInGoogleMaps()
        }

        binding.btnOpenWaze.setOnClickListener {
            openInWaze()
        }

        checkLocationPermission()

        return root
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            getLastLocation()
        } else {

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getLastLocation()
            } else {

                binding.textHome.text = getString(R.string.text_location_permission_denied)
                Toast.makeText(context, "Permissão de localização negada. O mapa não funcionará.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getLastLocation() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location: Location? ->
                if (location != null) {
                    receiveLocationUpdate(location)
                } else {
                    binding.textHome.text = getString(R.string.text_location_not_found)
                }
            }
        }
    }

    fun receiveLocationUpdate(location: Location) {
        lastKnownLocation = location
        binding.textHome.text = "Localização Atual:\nLat: %.6f, Long: %.6f".format(location.latitude, location.longitude)
    }

    private fun openInGoogleMaps() {
        val location = lastKnownLocation
        if (location == null) {
            Toast.makeText(context, "Localização não disponível. Aguarde o GPS.", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(Minha+Localização)"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(context, "Google Maps não está instalado.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openInWaze() {
        val location = lastKnownLocation
        if (location == null) {
            Toast.makeText(context, "Localização não disponível. Aguarde o GPS.", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = "https://waze.com/ul?ll=${location.latitude},${location.longitude}&navigate=yes"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.waze")

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(context, "Waze não está instalado. Não é possível abrir o Waze.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
