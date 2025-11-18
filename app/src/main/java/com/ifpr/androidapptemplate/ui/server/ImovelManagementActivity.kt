package com.ifpr.androidapptemplate.ui.server

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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
import com.ifpr.androidapptemplate.model.Imovel
import com.ifpr.androidapptemplate.model.Notification
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class ImovelManagementActivity : AppCompatActivity() {

    private lateinit var edtTitulo: EditText
    private lateinit var edtPreco: EditText
    private lateinit var spinnerModalidade: Spinner
    private lateinit var spinnerTipo: Spinner
    private lateinit var edtCep: EditText
    private lateinit var btnBuscarCep: Button
    private lateinit var edtEndereco: EditText
    private lateinit var edtNumero: EditText
    private lateinit var edtQuartos: EditText
    private lateinit var edtBanheiros: EditText
    private lateinit var edtMetragem: EditText
    private lateinit var btnSalvar: Button
    private lateinit var imgImovel: ImageView
    private lateinit var btnSelectPhoto: Button
    private lateinit var btnVerMapa: Button

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private var base64Image: String? = null

    private var isEditMode = false
    private var imovelId: String? = null
    private var currentImovel: Imovel? = null

    private lateinit var requestQueue: RequestQueue

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
        private const val REQUEST_PICK_IMAGE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imovel_management)

        edtTitulo = findViewById(R.id.edt_imovel_titulo)
        edtPreco = findViewById(R.id.edt_imovel_preco)
        spinnerModalidade = findViewById(R.id.spinner_imovel_modalidade)
        spinnerTipo = findViewById(R.id.spinner_imovel_tipo)
        edtCep = findViewById(R.id.edt_imovel_cep)
        btnBuscarCep = findViewById(R.id.btn_buscar_cep)
        edtEndereco = findViewById(R.id.edt_imovel_endereco)
        edtNumero = findViewById(R.id.edt_imovel_numero)
        edtQuartos = findViewById(R.id.edt_imovel_quartos)
        edtBanheiros = findViewById(R.id.edt_imovel_banheiros)
        edtMetragem = findViewById(R.id.edt_imovel_metragem)
        btnSalvar = findViewById(R.id.btn_salvar_imovel)
        imgImovel = findViewById(R.id.img_imovel_preview)
        btnSelectPhoto = findViewById(R.id.btn_select_photo)
        btnVerMapa = findViewById(R.id.btn_ver_mapa)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        requestQueue = Volley.newRequestQueue(this)

        setupSpinners()

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
        btnBuscarCep.setOnClickListener { buscarEnderecoPorCep() }
        btnVerMapa.setOnClickListener { verNoMapa() }
    }

    private fun setupSpinners() {
        val modalidades = arrayOf("Venda", "Aluguel")
        val modalidadeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, modalidades)
        modalidadeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerModalidade.adapter = modalidadeAdapter

        val tipos = arrayOf("Casa", "Apartamento", "Condomínio")
        val tipoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tipos)
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = tipoAdapter
    }

    private fun setupEditMode() {
        supportActionBar?.title = "Editar Imóvel"
        btnSalvar.text = "Atualizar"

        imovelId?.let { id ->
            val databaseRef = FirebaseDatabase.getInstance().getReference("destaques").child(id)
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentImovel = snapshot.getValue(Imovel::class.java)
                    currentImovel?.let { preencherFormulario(it) }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ImovelManagementActivity, "Falha ao carregar dados do imóvel.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun preencherFormulario(imovel: Imovel) {
        edtTitulo.setText(imovel.titulo)
        edtEndereco.setText(imovel.endereco)
        edtNumero.setText(imovel.numero)
        edtQuartos.setText(imovel.quartos.toString())
        edtBanheiros.setText(imovel.banheiros.toString())
        edtMetragem.setText(imovel.metragem.toString())

        val modalidades = arrayOf("Venda", "Aluguel")
        val modalidadePosition = modalidades.indexOf(imovel.modalidade)
        if (modalidadePosition >= 0) {
            spinnerModalidade.setSelection(modalidadePosition)
        }

        val tipos = arrayOf("Casa", "Apartamento", "Condomínio")
        val tipoPosition = tipos.indexOf(imovel.tipo)
        if (tipoPosition >= 0) {
            spinnerTipo.setSelection(tipoPosition)
        }

        val formatador = NumberFormat.getInstance(Locale("pt", "BR"))
        formatador.minimumFractionDigits = 2
        edtPreco.setText(formatador.format(imovel.preco))

        val firstBase64 = imovel.base64Images.firstOrNull()
        val firstUrl = imovel.imageUrls.firstOrNull()

        if (firstBase64 != null) {
            try {
                val bytes = Base64.decode(firstBase64, Base64.DEFAULT)
                imgImovel.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                base64Image = firstBase64 // Store for saving if not changed
            } catch (e: Exception) {
                 if (firstUrl != null) {
                    Glide.with(this).load(firstUrl).into(imgImovel)
                } else {
                    imgImovel.setImageResource(R.drawable.placeholder_image)
                }
            }
        } else if (firstUrl != null) {
            Glide.with(this).load(firstUrl).into(imgImovel)
        } else {
            imgImovel.setImageResource(R.drawable.placeholder_image)
        }
    }

    private fun salvarImovel() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val titulo = edtTitulo.text.toString().trim()
        val precoStr = edtPreco.text.toString().trim()
        val modalidade = spinnerModalidade.selectedItem.toString()
        val tipo = spinnerTipo.selectedItem.toString()
        val endereco = edtEndereco.text.toString().trim()
        val numero = edtNumero.text.toString().trim()
        val quartos = edtQuartos.text.toString().toIntOrNull() ?: 0
        val banheiros = edtBanheiros.text.toString().toIntOrNull() ?: 0
        val metragem = edtMetragem.text.toString().toDoubleOrNull() ?: 0.0

        if (titulo.isEmpty() || precoStr.isEmpty() || endereco.isEmpty() || numero.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show()
            return
        }

        val cleanedPrecoStr = precoStr.replace(".", "").replace(",", ".")
        val precoDouble = cleanedPrecoStr.toDoubleOrNull() ?: run {
            Toast.makeText(this, "Preço inválido.", Toast.LENGTH_LONG).show()
            return
        }

        if (isEditMode) {
            atualizarImovel(uid, titulo, modalidade, tipo, precoDouble, endereco, numero, quartos, banheiros, metragem)
        } else {
            criarNovoImovel(uid, titulo, modalidade, tipo, precoDouble, endereco, numero, quartos, banheiros, metragem)
        }
    }

    private fun criarNovoImovel(uid: String, titulo: String, modalidade: String, tipo: String, preco: Double, endereco: String, numero: String, quartos: Int, banheiros: Int, metragem: Double) {
        val location = lastKnownLocation ?: run {
            Toast.makeText(this, "Localização não disponível. Tente novamente.", Toast.LENGTH_SHORT).show()
            getLastLocation()
            return
        }
        val db = FirebaseDatabase.getInstance()
        val imoveisRef = db.getReference("imoveis").child(uid)
        val newImovelId = imoveisRef.push().key ?: UUID.randomUUID().toString()

        val imovel = Imovel(
            key = newImovelId, userId = uid, titulo = titulo, modalidade = modalidade, tipo = tipo, preco = preco, quartos = quartos,
            banheiros = banheiros, metragem = metragem, endereco = endereco, numero = numero, latitude = location.latitude,
            longitude = location.longitude, base64Images = if (base64Image != null) listOf(base64Image!!) else emptyList(),
            timestamp = System.currentTimeMillis()
        )

        val imovelSaveTask = imoveisRef.child(newImovelId).setValue(imovel)
        val destaqueSaveTask = db.getReference("destaques").child(newImovelId).setValue(imovel)

        Tasks.whenAll(imovelSaveTask, destaqueSaveTask).addOnSuccessListener {
            criarNotificacao(titulo, preco)
            finishWithMessage("Imóvel cadastrado com sucesso!")
        }.addOnFailureListener { e ->
            finishWithMessage("Erro ao cadastrar: ${e.message}")
        }
    }

    private fun criarNotificacao(tituloImovel: String, precoImovel: Double) {
        val notificationsRef = FirebaseDatabase.getInstance().getReference("notifications")
        val newNotificationId = notificationsRef.push().key ?: return

        val precoFormatado = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(precoImovel)

        val notification = Notification(
            id = newNotificationId,
            content = tituloImovel,
            price = precoFormatado
        )

        notificationsRef.child(newNotificationId).setValue(notification)
    }

    private fun atualizarImovel(uid: String, titulo: String, modalidade: String, tipo: String, preco: Double, endereco: String, numero: String, quartos: Int, banheiros: Int, metragem: Double) {
        val id = imovelId ?: return

        val imagesToSave = if (base64Image != null) listOf(base64Image!!) else currentImovel?.base64Images ?: emptyList()

        val updates = mutableMapOf<String, Any?>(
            "titulo" to titulo,
            "modalidade" to modalidade,
            "tipo" to tipo,
            "preco" to preco,
            "endereco" to endereco,
            "numero" to numero,
            "quartos" to quartos,
            "banheiros" to banheiros,
            "metragem" to metragem,
            "base64Images" to imagesToSave,
            "timestamp" to System.currentTimeMillis()
        )

        val db = FirebaseDatabase.getInstance()
        val imovelRef = db.getReference("imoveis").child(uid).child(id)
        val destaqueRef = db.getReference("destaques").child(id)

        Tasks.whenAll(imovelRef.updateChildren(updates), destaqueRef.updateChildren(updates))
            .addOnSuccessListener { finishWithMessage("Imóvel atualizado com sucesso!") }
            .addOnFailureListener { e -> finishWithMessage("Erro ao atualizar: ${e.message}") }
    }
    
    private fun finishWithMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            getLastLocation()
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            lastKnownLocation = location
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation()
        }
    }

    private fun buscarEnderecoPorCep() {
        val cep = edtCep.text.toString().trim()
        if (cep.length != 8) {
            Toast.makeText(this, "CEP inválido.", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://viacep.com.br/ws/$cep/json/"
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.has("erro")) {
                        Toast.makeText(this, "CEP não encontrado.", Toast.LENGTH_SHORT).show()
                    } else {
                        val logradouro = jsonObject.getString("logradouro")
                        val bairro = jsonObject.getString("bairro")
                        val cidade = jsonObject.getString("localidade")
                        val estado = jsonObject.getString("uf")
                        edtEndereco.setText("$logradouro, $bairro, $cidade - $estado")
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao processar o CEP.", Toast.LENGTH_SHORT).show()
                }
            },
            { Toast.makeText(this, "Erro na busca do CEP.", Toast.LENGTH_SHORT).show() })

        requestQueue.add(stringRequest)
    }

    private fun verNoMapa() {
        val endereco = edtEndereco.text.toString().trim()
        val numero = edtNumero.text.toString().trim()
        if (endereco.isEmpty()) {
            Toast.makeText(this, "O endereço não está preenchido.", Toast.LENGTH_SHORT).show()
            return
        }

        val fullAddress = if (numero.isNotEmpty()) "$endereco, $numero" else endereco
        val gmmIntentUri = Uri.parse("geo:0,0?q=$fullAddress")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(this, "Nenhum app de mapa encontrado.", Toast.LENGTH_SHORT).show()
        }
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
