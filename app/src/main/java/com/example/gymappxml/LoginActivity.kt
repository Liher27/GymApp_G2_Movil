package com.example.gymappxml

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passEditText: EditText
    private lateinit var db: FirebaseFirestore
    private lateinit var useid: String
    private lateinit var rememberMe: CheckBox
    private lateinit var perf: SharedPreferences
    private var saveUser by Delegates.notNull<Boolean>()
    private lateinit var editor: Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        perf = getSharedPreferences("document_sharedPreferences", MODE_PRIVATE)
        saveUser = perf.getBoolean("saveLogin", false)
        editor = perf.edit()
        emailEditText = findViewById(R.id.editTextLogin)
        passEditText = findViewById(R.id.editTextPassword)


        db = FirebaseFirestore.getInstance()
        rememberMe = findViewById(R.id.checkBox)


        if (saveUser) {
            emailEditText.setText(perf.getString("mail", null))
            passEditText.setText(perf.getString("pass", null))
            rememberMe.isChecked
        }


        val button: Button = findViewById(R.id.button)
        button.setOnClickListener {


            val email = emailEditText.text.toString()
            val password = passEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                checkUserCredentials(email, password)


            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        val register: TextView = findViewById(R.id.textViewRegister)
        register.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun checkUserCredentials(mail: String, pass: String) {
        db.collection("users").whereEqualTo("mail", mail)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    var isAuthenticated = false
                    val document = result.documents[0]
                    val userPassword = document.getString("pass")
                    if (userPassword.equals(pass, false)) {
                        isAuthenticated = true
                        useid = document.id
                        val intentProfileActivity =
                            Intent(this, WorkoutsActivity::class.java).apply {
                                putExtra("id", useid)
                            }
                        startActivity(intentProfileActivity)
                        if (rememberMe.isChecked) {
                            editor.putBoolean("saveLogin", true)
                            editor.putString("mail", mail)
                            editor.putString("pass", pass)
                            editor.apply()
                        } else {
                            editor.clear()
                            editor.apply()
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
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "No hay usuarios registrados con ese correo",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
            .addOnFailureListener {
                Toast.makeText(
                    this@LoginActivity,
                    "Error al leer datos de Firestore",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}


