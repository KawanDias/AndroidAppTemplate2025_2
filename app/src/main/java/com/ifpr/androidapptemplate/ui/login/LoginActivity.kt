package com.ifpr.androidapptemplate.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast // Import necessário para mostrar mensagens
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.MainActivity // Importa a MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Inicializa as views
        emailEditText = findViewById(R.id.edit_text_email)
        passwordEditText = findViewById(R.id.edit_text_password)
        loginButton = findViewById(R.id.button_login)
        registerLink = findViewById(R.id.registerLink)

        // 2. Configura a lógica de clique do botão de login
        loginButton.setOnClickListener {
            performLogin()
        }

        // --- CÓDIGO CORRIGIDO: O link Cadastre-se não faz nada agora (comportamento padrão) ---
        // Se no futuro você implementar uma tela de cadastro (RegisterActivity),
        // a lógica seria adicionada aqui. Por enquanto, ele apenas mostra uma mensagem:
        registerLink.setOnClickListener {
            Toast.makeText(this, "Funcionalidade de cadastro em desenvolvimento.", Toast.LENGTH_SHORT).show()
        }
        // --- FIM DO CÓDIGO CORRIGIDO ---
    }

    // 3. Função para executar a lógica de login
    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Verifica se os campos estão preenchidos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        // Simulação de autenticação com credenciais fixas
        // SE AS CREDENCIAIS FOREM CORRETAS:
        if (email == "teste@teste.com" && password == "123456") {

            // Login Bem-Sucedido: Navega para a MainActivity
            Toast.makeText(this, "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Fecha a tela de login

        } else {
            // Login Falhou
            Toast.makeText(this, "Credenciais inválidas. Tente novamente.", Toast.LENGTH_LONG).show()
        }
    }
}
