package com.example.gymappxml

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import pojo.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegisterActivity : AppCompatActivity() {

    private lateinit var buttonRegister: Button
    private lateinit var registerNameEditText : EditText
    private lateinit var registerSurnameEditText :EditText
    private lateinit var registerEmailEditText : EditText
    private lateinit var registerPasswordEditText : EditText
    private lateinit var registerDateEditText : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        buttonRegister = findViewById(R.id.button2)
        registerNameEditText = findViewById(R.id.editTextname)
        registerSurnameEditText = findViewById(R.id.editTextSurname)
        registerEmailEditText = findViewById(R.id.editTextTextEmailAddress)
        registerPasswordEditText = findViewById(R.id.editTextTextPassword)
        registerDateEditText = findViewById(R.id.editTextDate)

        var istrainer: Boolean

        Firebase.firestore
        val spinner : Spinner = findViewById(R.id.spinner)
        ArrayAdapter.createFromResource(
            this,R.array.userType,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        val register :TextView = findViewById(R.id.textView)
        register.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        buttonRegister.setOnClickListener {

            val registerName = registerNameEditText.text.toString()
            val registerSurname = registerSurnameEditText.text.toString()
            val registerEmail = registerEmailEditText.text.toString()
            val registerPassword = registerPasswordEditText.text.toString()
            val registerDate = registerDateEditText.text.toString()
            val level = 0
            istrainer = if(spinner.selectedItem.equals("Cliente")){
                false
            }else{
                true
            }
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = try {
                dateFormat.parse(registerDate)
            } catch (e: Exception) {
                null
            }
            if(registerName.isNotEmpty()&&registerSurname.isNotEmpty()&&registerEmail.isNotEmpty()&&registerPassword.isNotEmpty()&&
                registerDate.isNotEmpty()){
                userNames (registerName,
                    registerSurname,
                    registerEmail,
                    registerPassword,
                    istrainer,
                    date,
                    level)
            }
            else{
                Toast.makeText(this,"Hay campos que estan vacios,Rellenarlos por favor",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun userNames(name: String, surname: String, mail: String, pass: String, trainer: Boolean, date: Date?, level : Int) {
        val db = Firebase.firestore

        db.collection("users").whereEqualTo("mail", mail).get().addOnSuccessListener { querySnapshot ->
            when {
                querySnapshot.isEmpty -> {
                    val newUser = User(name, surname, mail, pass, trainer, date,level)
                    db.collection("users").document(name).set(newUser)
                    Toast.makeText(this,"Se ha registrado correctamente",Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this,"El correo electronico ya esta utilizado",Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener { e ->

        }
    }
}


