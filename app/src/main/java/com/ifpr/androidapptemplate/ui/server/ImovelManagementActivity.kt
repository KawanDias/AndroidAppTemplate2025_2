package com.ifpr.androidapptemplate.ui.server

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
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

    // Variáveis para modo de edição
    private var isEditMode = false
    private var imovelId: String? = null
    private var currentImovel: Item? = null

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_PICK_IMAGE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imovel_management)

        edtTitulo = findViewById(R.id.edt_imovel_titulo)
        edtPreco = findViewById(R.id.edt_imovel_preco)
        edtTipo = findViewById(R.id.edt_imovel_tipo)
        btnSalvar = findViewById(R.id.btn_salvar_imovel)
        imgImovel = findViewById(R.id.img_imovel_preview)
        btnSelectPhoto = findViewById(R.id.btn_select_photo)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verifica se está em modo de edição
        if (intent.hasExtra("IMOVEL_ID")) {
            isEditMode = true
            imovelId = intent.getStringExtra("IMOVEL_ID")
            setupEditMode()
        } else {
            supportActionBar?.title = "Cadastrar Imóvel"
            requestLocationPermission()
        }

        btnSelectPhoto.setOnClickListener { selectImage() }
        btnSalvar.setOnClickListener { salvarImovel() }
    }

    private fun setupEditMode() {
        supportActionBar?.title = "Editar Imóvel"
        btnSalvar.text = "Atualizar"

        imovelId?.let { id ->
            // Em modo de edição, carregamos os dados do nó 'destaques' que é público
            val databaseRef = FirebaseDatabase.getInstance().getReference("destaques").child(id)
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentImovel = snapshot.getValue(Item::class.java)
                    currentImovel?.let { preencherFormulario(it) }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ImovelManagementActivity, "Falha ao carregar dados do imóvel.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun preencherFormulario(item: Item) {
        edtTitulo.setText(item.titulo)
        edtTipo.setText(item.tipo)

        val formatador = NumberFormat.getInstance(Locale("pt", "BR"))
        formatador.minimumFractionDigits = 2
        edtPreco.setText(formatador.format(item.preco ?: 0.0))

        // Carrega a imagem existente
        if (!item.base64Image.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
                imgImovel.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            } catch (e: Exception) {
                 if (!item.imageUrl.isNullOrEmpty()) {
                    Glide.with(this).load(item.imageUrl).into(imgImovel)
                } else {
                    imgImovel.setImageResource(R.drawable.placeholder_image)
                }
            }
        } else if (!item.imageUrl.isNullOrEmpty()) {
            Glide.with(this).load(item.imageUrl).into(imgImovel)
        } else {
            imgImovel.setImageResource(R.drawable.placeholder_image)
        }
    }

    private fun salvarImovel() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show()
            return
        }

        val titulo = edtTitulo.text.toString().trim()
        val precoStr = edtPreco.text.toString().trim()
        val tipo = edtTipo.text.toString().trim()

        if (titulo.isEmpty() || precoStr.isEmpty() || tipo.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val cleanedPrecoStr = precoStr.replace(".", "").replace(",", ".")
        val precoDouble = cleanedPrecoStr.toDoubleOrNull() ?: run {
            Toast.makeText(this, "Preço inválido.", Toast.LENGTH_LONG).show()
            return
        }

        if (isEditMode) {
            atualizarImovel(uid, titulo, tipo, precoDouble)
        } else {
            criarNovoImovel(uid, titulo, tipo, precoDouble)
        }
    }

    private fun criarNovoImovel(uid: String, titulo: String, tipo: String, preco: Double) {
        val location = lastKnownLocation ?: run {
            Toast.makeText(this, "Localização não disponível. Tente novamente.", Toast.LENGTH_SHORT).show()
            getLastLocation()
            return
        }
        val endereco = getAddressFromLocation(location)

        val imoveisRef = FirebaseDatabase.getInstance().getReference("imoveis").child(uid)
        val newImovelId = imoveisRef.push().key ?: UUID.randomUUID().toString()

        val imovel = Item(
            key = newImovelId, userId = uid, titulo = titulo, endereco = endereco, tipo = tipo, preco = preco,
            latitude = location.latitude, longitude = location.longitude, base64Image = base64Image
        )

        val destaquesRef = FirebaseDatabase.getInstance().getReference("destaques").child(newImovelId)

        Tasks.whenAll(destaquesRef.setValue(imovel), imoveisRef.child(newImovelId).setValue(imovel))
            .addOnSuccessListener { finishWithMessage("Imóvel cadastrado com sucesso!") }
            .addOnFailureListener { e -> finishWithMessage("Erro ao cadastrar: ${e.message}") }
    }

    private fun atualizarImovel(uid: String, titulo: String, tipo: String, preco: Double) {
        val id = imovelId ?: return

        val updates = mutableMapOf<String, Any?>(
            "titulo" to titulo,
            "tipo" to tipo,
            "preco" to preco
        )
        // Se uma nova imagem foi selecionada, adiciona aos updates
        if (base64Image != null) {
            updates["base64Image"] = base64Image
        }

        val db = FirebaseDatabase.getInstance()
        val imovelRef = db.getReference("imoveis").child(uid).child(id)
        val destaqueRef = db.getReference("destaques").child(id)

        val imovelUpdateTask = imovelRef.updateChildren(updates)
        val destaqueUpdateTask = destaqueRef.updateChildren(updates)

        Tasks.whenAll(imovelUpdateTask, destaqueUpdateTask)
            .addOnSuccessListener { finishWithMessage("Imóvel atualizado com sucesso!") }
            .addOnFailureListener { e -> finishWithMessage("Erro ao atualizar: ${e.message}") }
    }
    
    private fun finishWithMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    // --- Métodos de localização, permissão e imagem (sem alterações) ---
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            lastKnownLocation = location
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        }
    }

    private fun getAddressFromLocation(location: Location): String? {
        val geocoder = Geocoder(this, Locale.getDefault())
        return try {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) { null }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    @Deprecated("onActivityResult is deprecated")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_PICK_IMAGE) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                try {
                    @Suppress("DEPRECATION")
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                    processImage(bitmap)
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao carregar imagem.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processImage(bitmap: Bitmap) {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 400, (bitmap.height * (400.0 / bitmap.width)).toInt(), true)
        imgImovel.setImageBitmap(scaledBitmap)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }
}