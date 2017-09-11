package com.gobbledygook.theawless.speechy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigationDrawerOpen, R.string.navigationDrawerClose)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        fragmentManager.beginTransaction().replace(R.id.fragmentHolder, MainFragment()).commit()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> fragmentManager.beginTransaction().replace(R.id.fragmentHolder, MainFragment()).commit()
            R.id.train -> fragmentManager.beginTransaction().replace(R.id.fragmentHolder, TrainFragment()).commit()
            R.id.github -> {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/theawless/Speechy"))
                startActivity(i)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
