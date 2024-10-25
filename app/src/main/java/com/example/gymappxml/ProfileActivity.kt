package com.example.gymappxml

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.intl.Locale
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import pojo.users
import java.text.SimpleDateFormat
import kotlin.text.format

class ProfileActivity : AppCompatActivity() {
    private lateinit var userFiled : EditText
    private lateinit var userNameFiled : EditText
    private lateinit var userSurnameFiled : EditText
    private lateinit var userEmailFiled : EditText
    private lateinit var userBirtyDateFiled : EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userFiled = findViewById(R.id.userIdFiled)
        userNameFiled = findViewById(R.id.NameFiled)
        userSurnameFiled = findViewById(R.id.SurNameFiled)
        userEmailFiled = findViewById(R.id.emailFiled)
        userBirtyDateFiled = findViewById(R.id.dateFiled)

        val button : Button = findViewById(R.id.button3)

        button.setOnClickListener {
            showUserData()
        }


    }




    private fun showUserData() {
        val db = Firebase.firestore
        val userId = intent.getStringExtra("iduser")

        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userName = document.getString("name")
                        val userSurname = document.getString("surname")
                        val userEmail = document.getString("mail")
                        val userBirtyDate = document.getDate("birtyDate")

                        userFiled.setText(userId)
                        userNameFiled.setText(userName)
                        userSurnameFiled.setText(userSurname)
                        userEmailFiled.setText(userEmail)

                        val dateFormat = SimpleDateFormat("dd/MM/yyyy",
                            java.util.Locale.getDefault())
                        val formattedBirthDate = userBirtyDate?.let { dateFormat.format(it) } ?: ""
                        userBirtyDateFiled.setText(formattedBirthDate)

                        Log.i("attr", "userName: $userName")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("showUserData", "Error getting user data: ", exception)

                }
        }
    }
}