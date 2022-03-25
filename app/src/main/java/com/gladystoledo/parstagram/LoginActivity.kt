package com.gladystoledo.parstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.parse.ParseObject
import com.parse.ParseUser

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Check if there's a user logged in
        //If there is , take them to MainActivity
        val currentUser = ParseUser.getCurrentUser()
        if (currentUser != null) {
            goToMainActivity()
        }

//Testing Connection - Success 3/25/2022 @2:22am cdt
//        val firstObject = ParseObject("GladysInstagram")
//        firstObject.put("username","test")
//        firstObject.put("password","test")
//        firstObject.saveInBackground {
//            if (it != null){
//                it.localizedMessage?.let { message -> Log.e("LoginActivity", message) }
//            }else{
//                Log.d("LoginActivity","Object saved.")
//            }
//        }

        findViewById<Button>(R.id.login_button).setOnClickListener{
            val username = findViewById<EditText>(R.id.et_username).text.toString()
            val password = findViewById<EditText>(R.id.et_password).text.toString()
            loginUser(username,password)
        }

        findViewById<Button>(R.id.signUp_button).setOnClickListener{
            val username = findViewById<EditText>(R.id.et_username).text.toString()
            val password = findViewById<EditText>(R.id.et_password).text.toString()
            signUpUser(username,password)
        }
    }

    private fun signUpUser(username: String,password: String){
        // Create the ParseUser
        val user = ParseUser()

        // Set fields for the user to be created
        user.setUsername(username)
        user.setPassword(password)

        user.signUpInBackground { e ->
            if (e == null) {
                // User has successfully created a new account
                //Navigate the user to the mainActivity
                goToMainActivity()
                //Show a toast to indicate user successfully signed up for an account
                Toast.makeText(this,"Successfully Signed Up", Toast.LENGTH_SHORT).show()
            } else {
                //show a Toast to user sign up was not successful
                e.printStackTrace()
                Toast.makeText(this,"Error Signing Up", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(username: String, password: String){
        ParseUser.logInInBackground(username, password, ({ user, e ->
            if (user != null) {
                // Hooray!  The user is logged in.
                Log.i(TAG, "Sucessfully logged in user")
                goToMainActivity()
            } else {
                // Signup failed.  Look at the ParseException to see what happened.
                e.printStackTrace()
                Toast.makeText(this,"Error loggin in", Toast.LENGTH_SHORT).show()
            }})
        )
    }

    private fun goToMainActivity(){
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()

    }

    companion object{
        val TAG = "LoginActivity"
    }
}