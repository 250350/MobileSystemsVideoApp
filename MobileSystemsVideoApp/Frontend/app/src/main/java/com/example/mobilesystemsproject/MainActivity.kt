package com.example.mobilesystemsproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.mobilesystemsproject.fragments.HomeFragment
import com.example.mobilesystemsproject.fragments.SearchFragment
import com.example.mobilesystemsproject.fragments.CloudFragment
import com.example.mobilesystemsproject.dialogs.AddVideoDialog
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        TokenManager.init(this)

        bottomNav = findViewById(R.id.bottomNavigation)

        // Load home fragment by default
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_search -> {
                    loadFragment(SearchFragment())
                    true
                }
                R.id.nav_add -> {
                    showAddDialog()
                    false // Don't select this item
                }
                R.id.nav_cloud -> {
                    loadFragment(CloudFragment())
                    true
                }
                R.id.nav_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showAddDialog() {
        val dialog = AddVideoDialog()
        dialog.show(supportFragmentManager, "AddVideoDialog")
    }

    fun refreshHomeFragment() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment is HomeFragment) {
            currentFragment.refreshVideos()
        }
    }
    private fun logout() {
        TokenManager.clearAll()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
