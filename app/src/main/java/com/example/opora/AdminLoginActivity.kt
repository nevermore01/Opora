package com.example.opora

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        auth = FirebaseAuth.getInstance()

        val loginField: EditText = findViewById(R.id.adminLogin)
        val passwordField: EditText = findViewById(R.id.adminPassword)
        val loginButton: Button = findViewById(R.id.loginButton)

        loginButton.setOnClickListener {
            val login = loginField.text.toString()
            val password = passwordField.text.toString()

            if (login.isNotEmpty() && password.isNotEmpty()) {
                if (login == "admin@gmail.com" && password == "admin12") {
                    auth.signInWithEmailAndPassword(login, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val intent = Intent(this, AdminPanelActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Неправильний логін або пароль", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Неправильний логін або пароль", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Будь ласка, заповніть всі поля", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
