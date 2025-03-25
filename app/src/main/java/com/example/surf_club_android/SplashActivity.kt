package com.example.surf_club_android

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.surf_club_android.viewmodel.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModels()
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progressBar = findViewById(R.id.progressBar)

        lifecycleScope.launch {
            // Wait for the initial data to load
            splashViewModel.isLoadingComplete.collectLatest { isDone ->
                if (isDone) {
                    if (splashViewModel.hasError.value) {
                        Toast.makeText(
                            this@SplashActivity,
                            "Failed to load upcoming posts",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // Navigate to the appropriate activity based on the user's sign-in status
                    val targetActivity = if (splashViewModel.isUserSignedIn.value) {
                        Log.d("SplashActivity", "User is signed in")
                        MainActivity::class.java
                    } else {
                        Log.d("SplashActivity", "User is not signed in")
                        AuthActivity::class.java
                    }
                    // Start the target activity and finish the splash activity
                    startActivity(Intent(this@SplashActivity, targetActivity))
                    finish()
                }
            }
        }

        animateProgressBar()
    }

    private fun animateProgressBar() {
        lifecycleScope.launch {
            var progress = 0
            while (progress < 100) {
                delay(30)
                progress += 2
                progressBar.progress = progress
            }
        }
    }
}
