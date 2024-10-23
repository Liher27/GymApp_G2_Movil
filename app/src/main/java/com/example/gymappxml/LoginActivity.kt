package com.example.gymappxml

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore


class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passEditText: EditText
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()

        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {
            emailEditText = findViewById(R.id.editTextLogin)
            passEditText = findViewById(R.id.editTextPassword)

            val email = emailEditText.text.toString()
            val password = passEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                checkUserCredentials(email, password)
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        val registrer: TextView = findViewById(R.id.textViewRegister)
        registrer.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun checkUserCredentials(mail: String, pass: String) {
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                var isAuthenticated = false
                for (document in result) {
                    val userEmail = document.getString("mail")
                    val userPassword = document.getString("pass")

                    if (userEmail == mail && userPassword == pass) {
                        isAuthenticated = true
                        break
                    }
                }
                if (isAuthenticated) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Credenciales Correctas",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Credenciales incorrectas",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@LoginActivity,
                    "Error al leer datos de Firestore",
                    Toast.LENGTH_SHORT
                ).show()
                Log.w("FirestoreData", "Error getting documents.", exception)
            }
    }

}


