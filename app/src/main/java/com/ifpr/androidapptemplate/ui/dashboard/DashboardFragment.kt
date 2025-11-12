package com.ifpr.androidapptemplate.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // ✅ Novo método para selecionar imagem (substitui onActivityResult)
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                imageUri = result.data!!.data
                Glide.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .into(binding.imageItem)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("itens")

        // ✅ Garante que a imagem comece limpa (sem sobreposição)
        binding.imageItem.setImageDrawable(null)

        // Selecionar imagem
        binding.buttonSelectImage.setOnClickListener {
            openFileChooser()
        }

        // Salvar item
        binding.salvarItemButton.setOnClickListener {
            salvarItem()
        }

        return view
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun salvarItem() {
        val endereco = binding.enderecoItemEditText.text.toString().trim()

        if (endereco.isEmpty() || imageUri == null) {
            Toast.makeText(context, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            return
        }

        uploadImageToDatabase()
    }

    private fun uploadImageToDatabase() {
        try {
            val inputStream = context?.contentResolver?.openInputStream(imageUri!!)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
                val endereco = binding.enderecoItemEditText.text.toString().trim()

                val item = Item(endereco, base64Image)
                saveItemIntoDatabase(item)
            } else {
                Toast.makeText(context, "Erro ao ler imagem", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao processar imagem", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveItemIntoDatabase(item: Item) {
        val userId = auth.uid ?: run {
            Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val itemId = databaseReference.push().key
        if (itemId == null) {
            Toast.makeText(context, "Erro ao gerar ID do item", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference.child(userId).child(itemId).setValue(item)
            .addOnSuccessListener {
                Toast.makeText(context, "Item cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                binding.enderecoItemEditText.text?.clear()
                binding.imageItem.setImageDrawable(null)
                imageUri = null
            }
            .addOnFailureListener {
                Toast.makeText(context, "Falha ao cadastrar o item", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
