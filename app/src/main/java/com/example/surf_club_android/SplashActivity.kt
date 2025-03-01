//package com.example.surf_club_android
//
//import android.animation.ObjectAnimator
//import android.annotation.SuppressLint
//import android.content.Intent
//import android.os.Bundle
//import android.view.animation.AnticipateInterpolator
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.animation.doOnEnd
//import kotlinx.android.synthetic.main.activity_splash.*
//
//@SuppressLint("CustomSplashScreen")
//class SplashActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_splash)
//
//        // Animate the main logo
//        ivLogo.alpha = 0f
//        ivLogo.animate().alpha(1f).setDuration(1000).start()
//
//        // Animate the organization logos
//        llOrganizations.alpha = 0f
//        llOrganizations.animate().alpha(1f).setStartDelay(500).setDuration(1000).start()
//
//        // Animate the progress bar
//        val progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100)
//        progressAnimator.duration = 3000
//        progressAnimator.interpolator = AnticipateInterpolator()
//        progressAnimator.start()
//
//        progressAnimator.doOnEnd {
//            // Start your main activity here
//            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
//            finish()
//        }
//    }
//}