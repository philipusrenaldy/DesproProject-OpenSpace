package com.submission.openspace

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.submission.openspace.databinding.ActivityMainBinding

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var activeFragment: Int? = null
    private var nextFragment: Int? = null

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.navigation_map && activeFragment != 2) {
                val mapFrag = MapFragment.newInstance()
                nextFragment = 2
                openFragment(mapFrag)
                return@OnNavigationItemSelectedListener true
            } else if (item.itemId == R.id.navigation_home && activeFragment != 1) {
                val homeFragment = HomeFragment.newInstance()
                nextFragment = 1
                openFragment(homeFragment)
                return@OnNavigationItemSelectedListener true
            }
            false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)

        with(bottomNavigation) {
            if (savedInstanceState == null) {
                toHome()
            }
            setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        if (activeFragment!! > nextFragment!!) {
            transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right)
        } else if (activeFragment!! < nextFragment!!) {
            transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left)
        }
        activeFragment = nextFragment
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        val selectedItemId = bottomNavigation.selectedItemId

        if (selectedItemId != R.id.navigation_home) {
            toHome()
            super.onBackPressed()
        }
    }

    private fun toHome() {
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.selectedItemId = R.id.navigation_home
        activeFragment = 1
        nextFragment = 1
        openFragment(HomeFragment.newInstance())
    }
}