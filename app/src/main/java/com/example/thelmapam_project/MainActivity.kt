package com.example.thelmapam_project

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        // Secure Admin Tab Logic
        checkAdminStatus(bottomNav)
    }

    private fun checkAdminStatus(bottomNav: BottomNavigationView) {
        // FOR PRESENTATION: Always show the admin tab so you don't depend on network/Firestore during demo
        bottomNav.menu.findItem(R.id.nav_admin).isVisible = true
        
        // The original Firestore logic is kept here but won't hide the icon if it fails
        firestore.collection("config").document("admin_settings")
            .get()
            .addOnSuccessListener { document ->
                val isDashboardEnabled = document.getBoolean("show_dashboard") ?: true
                bottomNav.menu.findItem(R.id.nav_admin).isVisible = isDashboardEnabled
            }
            .addOnFailureListener {
                // If network fails, we keep it visible for your demo
                bottomNav.menu.findItem(R.id.nav_admin).isVisible = true
            }
    }
}
