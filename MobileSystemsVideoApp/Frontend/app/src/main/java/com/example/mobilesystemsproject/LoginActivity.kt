package com.example.mobilesystemsproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.mobilesystemsproject.models.LoginRequest
import retrofit2.Call
import com.example.mobilesystemsproject.models.LoginResponse
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    lateinit var usernameInput: EditText
    lateinit var passwordInput: EditText
    lateinit var loginBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_activity)

        // Initialize TokenManager
        TokenManager.init(this)

        // Check if already logged in
        if (TokenManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        loginBtn = findViewById(R.id.login_btn)

        loginBtn.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                loginUser(username, password)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        // Disable button during login
        loginBtn.isEnabled = false
        loginBtn.text = "Logging in..."
//        navigateToMain()

        val request = LoginRequest(email, password)
        RetrofitClient.instance.login(request).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                loginBtn.isEnabled = true
                loginBtn.text = "Login"

                Log.i("LOGIN_DEBUG", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token

                    if (!token.isNullOrEmpty()) {
                        // Save token and email
                        TokenManager.saveToken(token)
                        TokenManager.saveEmail(email)

                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        Log.i("LOGIN_DEBUG", "Token saved successfully")

                        navigateToMain()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Invalid email or password"
                        403 -> "Access denied"
                        404 -> "User not found"
                        else -> "Login failed: ${response.code()}"
                    }
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    Log.e("LOGIN_DEBUG", "Login failed: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loginBtn.isEnabled = true
                loginBtn.text = "Login"

                Log.e("LOGIN_DEBUG", "Connection failed", t)
                Toast.makeText(
                    this@LoginActivity,
                    "Connection error: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun navigateToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
