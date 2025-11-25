package com.ifpr.androidapptemplate.ui.usuario

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Usuario
import com.ifpr.androidapptemplate.databinding.FragmentPerfilUsuarioBinding
import com.ifpr.androidapptemplate.ui.login.LoginActivity
import java.io.ByteArrayOutputStream
import java.io.IOException

class PerfilUsuarioFragment : Fragment() {

    private var _binding: FragmentPerfilUsuarioBinding? = null
    private val binding get() = _binding!!

    private lateinit var usersReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var selectedImageBase64: String? = null

    private val keyboardVisibilityListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private val r = Rect()
        override fun onGlobalLayout() {
            view?.getWindowVisibleDisplayFrame(r)
            val screenHeight = view?.rootView?.height ?: 0
            val keypadHeight = screenHeight - r.bottom

            val navView = activity?.findViewById<BottomNavigationView>(R.id.nav_view)
            if (keypadHeight > screenHeight * 0.15) { // Keyboard is open
                navView?.visibility = View.GONE
            } else { // Keyboard is closed
                navView?.visibility = View.VISIBLE
            }
        }
    }


    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                if (isAdded) {
                    try {
                        val correctedBitmap = handleImageOrientation(selectedImageUri)

                        Glide.with(this)
                            .load(correctedBitmap)
                            .placeholder(R.drawable.ic_profile_black_24dp)
                            .into(binding.userProfileImageView)

                        val baos = ByteArrayOutputStream()
                        correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                        selectedImageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

                        Toast.makeText(context, "Foto selecionada. Clique em ATUALIZAR para salvar.", Toast.LENGTH_LONG).show()

                    } catch (e: Exception) {
                        Log.e("ImageProcessing", "Erro ao processar imagem", e)
                        Toast.makeText(context, "Erro ao processar a imagem.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun handleImageOrientation(uri: Uri): Bitmap {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val exifInterfaceInputStream = requireContext().contentResolver.openInputStream(uri)
        val exifInterface = exifInterfaceInputStream?.let { androidx.exifinterface.media.ExifInterface(it) }
        exifInterfaceInputStream?.close()

        val orientation = exifInterface?.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL) ?: androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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
        val currentUser = auth.currentUser
        if (currentUser == null) {
            if (isAdded) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
            return
        }

        usersReference = FirebaseDatabase.getInstance().getReference("users")

        setupUI(currentUser)
        loadUserProfile(currentUser)

        view.viewTreeObserver.addOnGlobalLayoutListener(keyboardVisibilityListener)
    }

    private fun setupUI(currentUser: FirebaseUser) {
        binding.emailEditText.isEnabled = false
        binding.emailEditText.setText(currentUser.email)

        binding.userProfileImageView.setImageResource(R.drawable.ic_profile_black_24dp)
        binding.userProfileImageView.setOnClickListener { openGallery() }
        binding.atualizarButton.setOnClickListener { updateUserProfile() }
        binding.sairButton.setOnClickListener { signOut() }
        binding.btnMeusImoveis.isEnabled = false
        binding.btnMeusImoveis.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_todosImoveisFragment)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
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

                        if (isAdded && it.fotoBase64?.isNotEmpty() == true) {
                            try {
                                val imageBytes = Base64.decode(it.fotoBase64, Base64.DEFAULT)
                                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                Glide.with(this@PerfilUsuarioFragment)
                                    .load(decodedImage)
                                    .placeholder(R.drawable.ic_profile_black_24dp)
                                    .into(binding.userProfileImageView)
                            } catch (e: Exception) {
                                Log.e("ImageDecode", "Erro ao decodificar imagem Base64", e)
                                binding.userProfileImageView.setImageResource(R.drawable.ic_profile_black_24dp)
                            }
                        }
                    }
                } else {
                    binding.nameEditText.setText(currentUser.displayName)
                }
                if(isAdded) {
                    binding.btnMeusImoveis.isEnabled = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Erro ao recuperar dados: ${error.message}")
                if (isAdded) {
                    Toast.makeText(context, "Erro ao carregar perfil", Toast.LENGTH_SHORT).show()
                    binding.btnMeusImoveis.isEnabled = true
                }
            }
        })
    }

    private fun updateUserProfile() {
        val name = binding.nameEditText.text.toString().trim()
        val endereco = binding.enderecoEditText.text.toString().trim()
        val currentUser = auth.currentUser ?: return

        if (selectedImageBase64 != null) {
            updateUserProfileData(currentUser, name, endereco, selectedImageBase64)
        } else {
            usersReference.child(currentUser.uid).child("fotoBase64").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val existingFotoBase64 = snapshot.getValue(String::class.java)
                    updateUserProfileData(currentUser, name, endereco, existingFotoBase64)
                }

                override fun onCancelled(error: DatabaseError) {
                    if (isAdded) Toast.makeText(context, "Falha ao manter foto existente.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateUserProfileData(currentUser: FirebaseUser, name: String, endereco: String, fotoBase64: String?) {
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
                    fotoBase64 = fotoBase64
                )
                saveUserToDatabase(user)
            } else {
                if (isAdded) Toast.makeText(context, "Falha ao atualizar o nome do perfil.", Toast.LENGTH_SHORT).show()
                Log.e("AuthUpdate", "Falha ao atualizar o perfil do Firebase Auth.", task.exception)
            }
        }
    }

    private fun saveUserToDatabase(usuario: Usuario) {
        usuario.key?.let {
            usersReference.child(it).setValue(usuario).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (isAdded) Toast.makeText(context, "Atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("FirebaseDatabase", "Falha ao salvar os dados.", task.exception)
                    if (isAdded) Toast.makeText(context, "Falha ao salvar os dados.", Toast.LENGTH_SHORT).show()
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
        view?.viewTreeObserver?.removeOnGlobalLayoutListener(keyboardVisibilityListener)
        activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.visibility = View.VISIBLE
        _binding = null
    }
}
