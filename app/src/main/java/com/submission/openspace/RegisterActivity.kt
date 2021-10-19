package com.submission.openspace

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.submission.openspace.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private var database = FirebaseDatabase.getInstance()
    private var myRef = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.btnSignUp.setOnClickListener{
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString().trim()

            when{
                email.isEmpty() -> {
                    binding.etEmailRegister.error = "This field is required"
                    binding.etEmailRegister.requestFocus()
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    binding.etPasswordRegister.error = "This field is required"
                    binding.etPasswordRegister.requestFocus()
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    binding.etPasswordRegister.error = "Password at least 6 character"
                    binding.etPasswordRegister.requestFocus()
                    return@setOnClickListener
                }
                else -> {
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val currentUser = mAuth.currentUser
                                myRef.child("Users").child(currentUser!!.uid).child("nama").setValue("Nama Anda")
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Register Successfully!", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            else {
                                Toast.makeText(applicationContext, "Register Failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}