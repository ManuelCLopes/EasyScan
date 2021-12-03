package com.sidm.easyscan.presentation.ui

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sidm.easyscan.R
import com.sidm.easyscan.presentation.ui.fragments.HomeFragment
import com.sidm.easyscan.presentation.ui.fragments.RepositoryFragment

class MainActivity : AppCompatActivity() {

    private val homeFragment by lazy { HomeFragment() }
    private val repositoryFragment by lazy { RepositoryFragment() }
    // 0 - HomeFragment    1 - RepositoryFragment
    private var currentFragment: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {

            loadFragment(homeFragment)
        }
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.home -> {
                    currentFragment = 0
                    loadFragment(homeFragment)
                    true
                }


                R.id.repository -> {
                    currentFragment = 1
                    loadFragment(repositoryFragment)
                    true
                }

                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (currentFragment == 0)
            menuInflater.inflate(R.menu.main_appbar, menu)

        return true
    }

    private fun loadFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.navigationContainer, fragment)
            .commit()
    }

}