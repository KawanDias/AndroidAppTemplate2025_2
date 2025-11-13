package com.ifpr.androidapptemplate.ui.usuario

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Usuario
import com.ifpr.androidapptemplate.databinding.FragmentPerfilUsuarioBinding
import com.ifpr.androidapptemplate.ui.login.LoginActivity

class PerfilUsuarioFragment : Fragment() {

    private var _binding: FragmentPerfilUsuarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var usersReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_profile_black_24dp)
                    .into(binding.userProfileImageView)

                Toast.makeText(context, "Foto selecionada localmente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilUsuarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        usersReference = FirebaseDatabase.getInstance().getReference("users")

        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
            return
        }

        setupUI(currentUser)
        loadUserProfile(currentUser)
    }

    private fun setupUI(currentUser: FirebaseUser) {
        binding.emailEditText.isEnabled = false
        binding.emailEditText.setText(currentUser.email)

        Glide.with(this)
            .load(currentUser.photoUrl)
            .placeholder(R.drawable.ic_profile_black_24dp)
            .into(binding.userProfileImageView)

        binding.userProfileImageView.setOnClickListener { openGallery() }
        binding.atualizarButton.setOnClickListener { updateUserProfile() }
        binding.sairButton.setOnClickListener { signOut() }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun loadUserProfile(currentUser: FirebaseUser) {
        usersReference.child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val usuario = snapshot.getValue(Usuario::class.java)
                    usuario?.let {
                        binding.nameEditText.setText(it.nomeCompleto)
                        binding.enderecoEditText.setText(it.endereco)
                        Glide.with(this@PerfilUsuarioFragment)
                            .load(it.fotoUrl)
                            .placeholder(R.drawable.ic_profile_black_24dp)
                            .into(binding.userProfileImageView)
                    }
                } else {
                    binding.nameEditText.setText(currentUser.displayName)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Erro ao recuperar dados: ${error.message}")
                Toast.makeText(context, "Erro ao carregar perfil", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserProfile() {
        val name = binding.nameEditText.text.toString().trim()
        val endereco = binding.enderecoEditText.text.toString().trim()
        val currentUser = auth.currentUser ?: return

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        currentUser.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = Usuario(
                    key = currentUser.uid,
                    nomeCompleto = name,
                    email = currentUser.email,
                    endereco = endereco,
                    fotoUrl = currentUser.photoUrl?.toString()
                )
                saveUserToDatabase(user)
            } else {
                Toast.makeText(context, "Falha ao atualizar o perfil.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserToDatabase(usuario: Usuario) {
        usuario.key?.let {
            usersReference.child(it).setValue(usuario).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("FirebaseDatabase", "Falha ao salvar os dados.", task.exception)
                    Toast.makeText(context, "Falha ao salvar os dados.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
