package com.example.surf_club_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.surf_club_android.databinding.ActivityAuthBinding
import com.example.surf_club_android.viewmodel.AuthViewModel

class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            showLoginFragment()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.user.observe(this) { user ->
            user?.let {
                // User is signed in, navigate to main activity
                navigateToMainActivity()
            }
        }
    }

    private fun showLoginFragment() {
        replaceFragment(LoginFragment())
    }

    private fun showRegisterFragment() {
        replaceFragment(RegisterFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun navigateToRegister() {
        showRegisterFragment()
    }

    fun navigateToLogin() {
        showLoginFragment()
    }

    private fun navigateToMainActivity() {
        // Replace MainActivity::class.java with your actual main activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}