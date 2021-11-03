package com.sidm.easyscan.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sidm.easyscan.R
import com.sidm.easyscan.presentation.ui.fragments.RepositoryFragment
import com.sidm.easyscan.presentation.ui.fragments.HomeFragment

class MainActivity : AppCompatActivity() {

    private val homeFragment by lazy { HomeFragment() }
    private val repositoryFragment by lazy { RepositoryFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(homeFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.home -> {
                    loadFragment(homeFragment)
                    true
                }


                R.id.about -> {
                    loadFragment(repositoryFragment)
                    true
                }

                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.navigationContainer, fragment)
            .commit()
    }

}