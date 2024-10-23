package com.example.gymappxml

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import pojo.users
import java.text.SimpleDateFormat
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        val buttonRegister: Button = findViewById(R.id.button2)
        val registerNameEditText = findViewById<EditText>(R.id.editTextname)
        val registerSurnameEditText = findViewById<EditText>(R.id.editTextSurname)
        val registerEmailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val registerPasswordEditText = findViewById<EditText>(R.id.editTextTextPassword)
        val registerDateEditText = findViewById<EditText>(R.id.editTextDate)
        var istrainer: String = ""
        val db = Firebase.firestore

        val spinner : Spinner = findViewById(R.id.spinner)




        buttonRegister.setOnClickListener {

            val registerName = registerNameEditText.text.toString()
            val registerSurname = registerSurnameEditText.text.toString()
            val registerEmail = registerEmailEditText.text.toString()
            val registerPassword = registerPasswordEditText.text.toString()
            val registerDate = registerDateEditText.text.toString()


            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = try {
                dateFormat.parse(registerDate)
            } catch (e: Exception) {
                null
            }


                    val newUser = users(
                        registerName,
                        registerSurname,
                        registerEmail,
                        registerPassword,
                        istrainer,
                        date
                    )
                    db.collection("users").document(registerName).set(newUser)
                }
        }
        }


fun userNames ( userData: users){
    val db = Firebase.firestore
    db.collection("users").get().addOnSuccessListener {
        result ->
        for (document in result){

        }
    }
}