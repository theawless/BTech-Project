package com.gobbledygook.theawless.speechy.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.gobbledygook.theawless.speechy.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private companion object {
        private const val REQUEST_PERMISSIONS_STATUS_CODE = 200
        private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
    }

    private var writeExternalPermission = false
    val speechDir by lazy {
        if (writeExternalPermission) getExternalFilesDir(null) else filesDir
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSIONS_STATUS_CODE -> {
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    writeExternalPermission = true
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    return
            }
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        writeExternalPermission = checkSelfPermission(permissions[0]) == PackageManager.PERMISSION_GRANTED
        requestPermissions(permissions, REQUEST_PERMISSIONS_STATUS_CODE)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigationDrawerOpen, R.string.navigationDrawerClose)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
        fragmentManager.beginTransaction().replace(R.id.fragmentHolder, MainFragment()).commit()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> fragmentManager.beginTransaction().replace(R.id.fragmentHolder, MainFragment()).commit()
            R.id.train -> fragmentManager.beginTransaction().replace(R.id.fragmentHolder, TrainFragment()).commit()
            R.id.github -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/theawless/Speechy")))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}