package com.submission.openspace

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var tb: Toolbar
    private var activeFragment: Int? = null
    private var nextFragment: Int? = null

    @Suppress("DEPRECATION")
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener{ item ->

        if(item.itemId == R.id.navigation_map && activeFragment != 2){

            return@OnNavigationItemSelectedListener true
        }

        else if(item.itemId == R.id.navigation_home && activeFragment != 1){
            tb.title = "PROFILE"
            val homeFragment = HomeFragment.newInstance()
            nextFragment = 1
            openFragment(homeFragment)
            return@OnNavigationItemSelectedListener true
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tb = findViewById(R.id.toolbarr)
        setSupportActionBar(tb)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)

        if(savedInstanceState == null){
            toHome()
        }

        tb.title = ""
        tb.setTitleTextColor(resources.getColor(R.color.transparent))

        // ini inisialisasi bottomNavBar nya
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun openFragment(fragment: Fragment){
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        if(activeFragment!! > nextFragment!!){
            transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right)
        }
        else if(activeFragment!! < nextFragment!!){
            transaction.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_left)
        }
        activeFragment = nextFragment
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun toHome(){
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigationView)
        bottomNavigation.selectedItemId = R.id.navigation_home
        activeFragment = 1
        nextFragment = 1
        tb.title = "PROFILE"
        tb.setTitleTextColor(resources.getColor(R.color.white))
        openFragment(HomeFragment.newInstance())
    }
}