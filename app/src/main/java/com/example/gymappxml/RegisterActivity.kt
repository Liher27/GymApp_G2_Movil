package com.example.gymappxml

import android.os.Bundle
import android.widget.ArrayAdapter
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

        var istrainer: String = ""

        val db = Firebase.firestore
        val spinner : Spinner = findViewById(R.id.spinner)
        ArrayAdapter.createFromResource(
            this,R.array.userType,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        buttonRegister.setOnClickListener {

            val registerName = registerNameEditText.text.toString()
            val registerSurname = registerSurnameEditText.text.toString()
            val registerEmail = registerEmailEditText.text.toString()
            val registerPassword = registerPasswordEditText.text.toString()
            val registerDate = registerDateEditText.text.toString()
            if(spinner.selectedItem.equals("Cliente")){
                istrainer = "Cliente"
            }else{
                istrainer = "Trainer"
            }
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = try {
                dateFormat.parse(registerDate)
            } catch (e: Exception) {
                null
            }
            if(registerName.isNotEmpty()&&registerSurname.isNotEmpty()&&registerEmail.isNotEmpty()&&registerPassword.isNotEmpty()&&
                registerDate.isNotEmpty()){
                val newUser = users(registerName,
                        registerSurname,
                        registerEmail,
                        registerPassword,
                        istrainer,
                        date)
                db.collection("users").document(registerName).set(newUser)
            }
            else{
                Toast.makeText(this,"Hay campos que estan vacios,Rellenarlos por favor",Toast.LENGTH_SHORT).show()
            }
                }
        }
        }
fun userNames ( name : String, surname:String,mail : String, pass : String , trainer : String,date : Date?){
    val db = Firebase.firestore
    db.collection("users").whereEqualTo("mail", mail).get().addOnSuccessListener {
        querySnapshot ->
        if (querySnapshot.isEmpty) {
            val newUser = users(
                name,
                surname,
                mail,
                pass,
                trainer,
                date
            )
            db.collection("users").document(name).set(newUser)
                .addOnSuccessListener {
                }
                .addOnFailureListener { e ->

                }
        } else {

        }
    }
        .addOnFailureListener { e ->

        }
}

