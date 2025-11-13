package com.ifpr.androidapptemplate.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.ifpr.androidapptemplate.MainActivity
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.ui.usuario.CadastroUsuarioActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var btnGoogleSignIn: SignInButton
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Não é necessário inicializar o FirebaseApp aqui se já estiver configurado na classe Application
        // FirebaseApp.initializeApp(this)

        firebaseAuth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.edit_text_email)
        passwordEditText = findViewById(R.id.edit_text_password)
        loginButton = findViewById(R.id.button_login)
        registerLink = findViewById(R.id.registerLink)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)

        registerLink.setOnClickListener {
            val intent = Intent(applicationContext, CadastroUsuarioActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnGoogleSignIn.setOnClickListener {
            signInGoogle()
        }
    }

    private fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    updateUI(firebaseAuth.currentUser)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Email ou senha incorretos.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a LoginActivity para que o usuário não volte para ela ao pressionar "voltar"
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithGoogle:success")
                    updateUI(firebaseAuth.currentUser)
                } else {
                    Log.w(TAG, "signInWithGoogle:failure", task.exception)
                    Toast.makeText(baseContext, "Falha na autenticação com o Google.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Falha no login com o Google.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}