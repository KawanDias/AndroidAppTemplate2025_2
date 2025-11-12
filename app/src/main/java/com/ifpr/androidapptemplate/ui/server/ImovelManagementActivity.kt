package com.ifpr.androidapptemplate.ui.server

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.ifpr.androidapptemplate.R

class ImovelManagementActivity : AppCompatActivity() {

    private lateinit var edtTitulo: EditText
    private lateinit var edtPreco: EditText
    private lateinit var edtTipo: EditText
    private lateinit var btnSalvar: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imovel_management)

        supportActionBar?.title = "Cadastrar Imóvel"

        // Inicializa os campos
        edtTitulo = findViewById(R.id.edt_imovel_titulo)
        edtPreco = findViewById(R.id.edt_imovel_preco)
        edtTipo = findViewById(R.id.edt_imovel_tipo)
        btnSalvar = findViewById(R.id.btn_salvar_imovel)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnSalvar.setOnClickListener {
            getCurrentLocationAndSaveImovel()
        }
    }

    private fun getCurrentLocationAndSaveImovel() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                saveImovelWithLocation(location)
            } else {
                Toast.makeText(this, "Não foi possível obter a localização atual", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            100
        )
    }

    private fun saveImovelWithLocation(location: Location) {
        val titulo = edtTitulo.text.toString()
        val preco = edtPreco.text.toString()
        val tipo = edtTipo.text.toString()
        val latitude = location.latitude
        val longitude = location.longitude

        // Aqui você pode salvar no banco, API etc.
        Toast.makeText(
            this,
            "Imóvel salvo:\n$titulo - $tipo - R$$preco\nLocalização: $latitude, $longitude",
            Toast.LENGTH_LONG
        ).show()
    }
}
