package com.ifpr.androidapptemplate.ui.server

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.UUID

class ImovelManagementActivity : AppCompatActivity() {

    private lateinit var edtTitulo: EditText
    private lateinit var edtPreco: EditText
    private lateinit var edtTipo: EditText
    private lateinit var btnSalvar: Button
    private lateinit var imgImovel: ImageView
    private lateinit var btnSelectPhoto: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private var base64Image: String? = null

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_PICK_IMAGE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imovel_management)

        supportActionBar?.title = "Cadastrar Imóvel"

        edtTitulo = findViewById(R.id.edt_imovel_titulo)
        edtPreco = findViewById(R.id.edt_imovel_preco)
        edtTipo = findViewById(R.id.edt_imovel_tipo)
        btnSalvar = findViewById(R.id.btn_salvar_imovel)
        imgImovel = findViewById(R.id.img_imovel_preview)
        btnSelectPhoto = findViewById(R.id.btn_select_photo)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestLocationPermission()

        btnSelectPhoto.setOnClickListener {
            selectImage()
        }

        btnSalvar.setOnClickListener {
            salvarImovel()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            lastKnownLocation = location
            if (location == null) {
                Toast.makeText(this, "Aguardando localização GPS...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getAddressFromLocation(location: Location): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            addresses?.firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) {
            Log.e("Geocoder", "Erro ao buscar endereço: ${e.message}")
            null
        }
    }

    private fun selectImage() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val chooserIntent = Intent.createChooser(pickPhotoIntent, "Selecionar Imagem")
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePictureIntent))
        }

        startActivityForResult(chooserIntent, REQUEST_PICK_IMAGE)
    }

    @Deprecated("Usado para compatibilidade com versões antigas")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_IMAGE -> {
                    val imageUri: Uri? = data?.data
                    if (imageUri != null) {
                        try {
                            @Suppress("DEPRECATION")
                            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                            processImage(bitmap)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Erro ao carregar imagem.", Toast.LENGTH_SHORT).show()
                            Log.e("ImovelManagement", "Erro ao carregar URI: ${e.message}")
                        }
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                    val photoBitmap = data?.extras?.get("data") as Bitmap?
                    if (photoBitmap != null) {
                        processImage(photoBitmap)
                    }
                }
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, (bitmap.height * (400.0 / bitmap.width)).toInt(), true)
        imgImovel.setImageBitmap(scaledBitmap)

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT)
        Toast.makeText(this, "Foto processada com sucesso!", Toast.LENGTH_SHORT).show()
    }

    private fun salvarImovel() {
        val titulo = edtTitulo.text.toString().trim()
        val precoStr = edtPreco.text.toString().trim()
        val tipo = edtTipo.text.toString().trim()

        if (titulo.isEmpty() || precoStr.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val precoDouble = precoStr.toDoubleOrNull() ?: run {
            Toast.makeText(this, "Preço inválido. Use apenas números (ex: 150000.00).", Toast.LENGTH_LONG).show()
            return
        }

        val location = lastKnownLocation ?: run {
            Toast.makeText(this, "Localização não disponível. Tente novamente após obter o GPS.", Toast.LENGTH_SHORT).show()
            getLastLocation()
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(this, "Usuário não autenticado. Por favor, faça login.", Toast.LENGTH_SHORT).show()
            return
        }

        val endereco = getAddressFromLocation(location)

        // ===== CORREÇÃO APLICADA AQUI =====
        // A referência agora aponta para um nó específico do usuário (imoveis/{userId})
        // Isso garante que a regra de segurança do Firebase seja respeitada.
        val imoveisRef = FirebaseDatabase.getInstance().getReference("imoveis").child(uid)
        val newImovelRef = imoveisRef.push()
        val imovelId = newImovelRef.key ?: UUID.randomUUID().toString()

        val imovel = Item(
            key = imovelId,
            userId = uid,
            titulo = titulo,
            endereco = endereco,
            tipo = tipo,
            preco = precoDouble,
            quartos = 0,
            banheiros = 0,
            metragem = 0.0,
            latitude = location.latitude,
            longitude = location.longitude,
            imageUrl = null,
            base64Image = base64Image
        )

        val destaquesRef = FirebaseDatabase.getInstance().getReference("destaques").child(imovelId)

        val saveImovelTask = newImovelRef.setValue(imovel)
        val saveDestaqueTask = destaquesRef.setValue(imovel)

        Tasks.whenAll(saveImovelTask, saveDestaqueTask)
            .addOnSuccessListener {
                Toast.makeText(this, "Imóvel e Destaque cadastrados com sucesso!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Log.e("FirebaseSave", "Erro ao salvar o imóvel: ${it.message}")
                Toast.makeText(this, "Erro ao salvar: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
