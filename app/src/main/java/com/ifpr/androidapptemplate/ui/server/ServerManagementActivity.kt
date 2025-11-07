// Caminho: com.ifpr.androidapptemplate.ui.server.ServerManagementActivity.kt

package com.ifpr.androidapptemplate.ui.server

import android.Manifest // NOVO IMPORT
import android.content.pm.PackageManager // NOVO IMPORT
import android.location.Location // NOVO IMPORT
import android.os.Bundle
import android.widget.Button // NOVO IMPORT
import android.widget.EditText // NOVO IMPORT
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat // NOVO IMPORT
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient // NOVO IMPORT
import com.google.android.gms.location.LocationServices // NOVO IMPORT
import com.google.firebase.auth.FirebaseAuth // NOVO IMPORT
import com.google.firebase.database.*
import com.ifpr.androidapptemplate.databinding.ActivityServerManagementBinding
import com.ifpr.androidapptemplate.model.Server
import com.ifpr.androidapptemplate.ui.server.adapter.ServerAdapter

class ServerManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServerManagementBinding
    private lateinit var serverAdapter: ServerAdapter
    private val serverList = mutableListOf<Server>()
    private val database = FirebaseDatabase.getInstance().getReference("servers")

    // --- NOVO: Variáveis para Localização e Status ---
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private lateinit var editTextStatus: EditText // Não é estritamente necessário se usar apenas binding
    private lateinit var btnShareStatus: Button // Não é estritamente necessário se usar apenas binding
    private val LOCATION_PERMISSION_REQUEST_CODE = 102
    // ----------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityServerManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        fetchServers()

        // --- NOVO: Inicializa a funcionalidade de Status Manual ---
        inicializaLocalizacaoEStatus()
        // -----------------------------------------------------------
    }

    // --- NOVO: Inicialização da UI e Localização ---
    private fun inicializaLocalizacaoEStatus() {
        // As variáveis globais não são estritamente necessárias se usarmos apenas o 'binding' aqui:
        // Exemplo: Não precisa destas linhas se usar 'binding.editTextStatus' diretamente:
        // editTextStatus = binding.editTextStatus
        // btnShareStatus = binding.btnShareStatus

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configura o clique do botão de compartilhamento
        binding.btnShareStatus.setOnClickListener { // USANDO BINDING
            getLastLocation()
        }

        // Verifica as permissões de localização
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            lastKnownLocation = location
            val lat = location?.latitude ?: 0.0
            val lng = location?.longitude ?: 0.0

            updateFirebaseStatusAndLocation(lat, lng)
        }
    }

    private fun updateFirebaseStatusAndLocation(lat: Double, lng: Double) {
        val statusText = binding.editTextStatus.text.toString().trim() // USANDO BINDING

        if (statusText.isEmpty()) {
            Toast.makeText(this, "Por favor, digite seu status (Ex: Em aula).", Toast.LENGTH_LONG).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Erro: Usuário não autenticado. Faça login novamente.", Toast.LENGTH_LONG).show()
            return
        }

        val databaseRef = database.child(userId)

        val updates = mapOf(
            "status" to statusText,
            "latitude" to lat,
            "longitude" to lng,
            "lastUpdated" to System.currentTimeMillis()
        )

        databaseRef.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Status e Localização atualizados!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Falha ao atualizar o Firebase: ${it.message}", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, "Permissão concedida. Clique no botão de Compartilhar novamente.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissão de localização é necessária para compartilhar.", Toast.LENGTH_LONG).show()
            }
        }
    }
    // ----------------------------------------------------

    private fun setupRecyclerView() {
        serverAdapter = ServerAdapter(serverList,
            onEdit = { server -> showEditDialog(server) },
            onDelete = { server -> deleteServer(server) }
        )
        binding.rvServers.layoutManager = LinearLayoutManager(this)
        binding.rvServers.adapter = serverAdapter
    }

    private fun setupListeners() {
        binding.fabAddServer.setOnClickListener {
            showAddDialog()
        }
        // NÃO PRECISA DE LISTENER AQUI, POIS FOI PARA inicializaLocalizacaoEStatus()
    }

    // --- Lógica de LEITURA (READ) com Firebase ---
    private fun fetchServers() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                serverList.clear()
                for (serverSnapshot in snapshot.children) {
                    val server = serverSnapshot.getValue(Server::class.java)
                    server?.id = serverSnapshot.key //
                    server?.let { serverList.add(it) }
                }
                serverAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ServerManagementActivity, "Falha ao carregar dados: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // --- Lógica de CRIAÇÃO (CREATE) ---
    private fun showAddDialog() {
        val editText = EditText(this)
        editText.hint = "Nome do novo Servidor/Professor"

        AlertDialog.Builder(this)
            .setTitle("Adicionar Servidor")
            .setView(editText)
            .setPositiveButton("Salvar") { dialog, which ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    addServer(name)
                } else {
                    Toast.makeText(this, "Nome não pode ser vazio.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun addServer(name: String) {
        val newServerRef = database.push()
        val newServer = Server(id = newServerRef.key, name = name)

        newServerRef.setValue(newServer)
            .addOnSuccessListener {
                Toast.makeText(this, "$name adicionado!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao adicionar servidor.", Toast.LENGTH_SHORT).show()
            }
    }

    // --- Lógica de ATUALIZAÇÃO (UPDATE) ---
    private fun showEditDialog(server: Server) {
        val editText = EditText(this)
        editText.setText(server.name)

        AlertDialog.Builder(this)
            .setTitle("Editar Servidor")
            .setView(editText)
            .setPositiveButton("Salvar") { dialog, which ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateServer(server.id!!, newName)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateServer(id: String, newName: String) {
        database.child(id).child("name").setValue(newName)
            .addOnSuccessListener {
                Toast.makeText(this, "Servidor atualizado!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao atualizar servidor.", Toast.LENGTH_SHORT).show()
            }
    }

    // --- Lógica de EXCLUSÃO (DELETE) ---
    private fun deleteServer(server: Server) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Servidor")
            .setMessage("Tem certeza que deseja excluir ${server.name}?")
            .setPositiveButton("Sim") { dialog, which ->
                server.id?.let { id ->
                    database.child(id).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Servidor excluído!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Erro ao excluir servidor.", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }
}