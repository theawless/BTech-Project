package com.gobbledygook.theawless.speechy

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.widget.CheckBox
import android.widget.Toast


class MainActivity : Activity() {
    private companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        private const val PERMISSIONS_STATUS_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)
        requestPermissions()
        stopService(Intent(applicationContext, FloatingService::class.java))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_STATUS_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            return
        }
        abortStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSIONS_STATUS_CODE && Settings.canDrawOverlays(this)) {
            return
        }
        abortStart()
    }

    private fun requestPermissions() {
        requestPermissions(PERMISSIONS, PERMISSIONS_STATUS_CODE)
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")), PERMISSIONS_STATUS_CODE)
        }
        if (!Settings.System.canWrite(applicationContext)) {
            startActivityForResult(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")), PERMISSIONS_STATUS_CODE)
        }
    }

    private fun abortStart() {
        Toast.makeText(this, R.string.permissionNotGratedToast, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        (menu.findItem(R.id.toggleItem).actionView as CheckBox).setOnCheckedChangeListener { _, checked ->
            if (checked) {
                startService(Intent(applicationContext, FloatingService::class.java))
            } else {
                stopService(Intent(applicationContext, FloatingService::class.java))
            }
        }
        return super.onCreateOptionsMenu(menu)
    }
}
