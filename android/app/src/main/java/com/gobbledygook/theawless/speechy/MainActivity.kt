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
import android.widget.RadioButton
import android.widget.Toast
import kotlinx.android.synthetic.main.activity.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.io.File


class MainActivity : Activity() {
    private companion object {
        private val PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        private const val PERMISSIONS_STATUS_CODE = 200
    }

    private val words by lazy {
        val file = File(Functions.getFilename(this, "sr-lib.words"))
        file.createNewFile()
        file.readLines()
    }
    private val wavRecorder = WavRecorder()

    init {
        System.loadLibrary("Speechy")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)
        requestPermissions()
        stopService(Intent(applicationContext, FloatingService::class.java))

        for (word in words) {
            val radio = RadioButton(this)
            radio.text = word
            wordRadioGroup.addView(radio)
            wordRadioGroup.check(radio.id)
        }
        recordRoundProgress.setState(true)
        recordRoundProgress.setOnClickListener {
            val word = words[wordRadioGroup.indexOfChild(findViewById(wordRadioGroup.checkedRadioButtonId))]
            var index = 0
            while (File(Functions.getFilename(this, "${word}_$index${wavRecorder.getExtension()}")).exists()) {
                index++
            }
            launch(UI) {
                recordRoundProgress.setState(false)
                recordRoundProgress.start(Constants.RECORD_DURATION)
                withContext(CommonPool) {
                    wavRecorder.record(Functions.getFilename(this@MainActivity, "${word}_$index"), Constants.RECORD_DURATION)
                }
                recordRoundProgress.setState(true)
            }
        }
        recordRoundProgress.setOnLongClickListener {
            launch(UI) {
                recordRoundProgress.setState(false)
                withContext(CommonPool) {
                    train(Functions.getFolder(this@MainActivity))
                }
                recordRoundProgress.setState(true)
            }
            true
        }
    }

    // only for testing purposes
    private external fun train(folder: String)

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
                finish()
            } else {
                stopService(Intent(applicationContext, FloatingService::class.java))
            }
        }
        return super.onCreateOptionsMenu(menu)
    }
}