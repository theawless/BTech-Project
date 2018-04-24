package com.gobbledygook.theawless.speechy

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.provider.Settings
import android.view.KeyEvent
import kotlin.math.max
import kotlin.math.min


class ActionPerformer(private val context: Context) {
    val actions: List<List<String>>
        get() {
            return listOf(listOf("bluetooth", "on"),
                          listOf("bluetooth", "off"),
                          listOf("wifi", "on"),
                          listOf("wifi", "off"),
                          listOf("torch", "on"),
                          listOf("torch", "off"),
                          listOf("call", "home"),
                          listOf("call", "emergency"),
                          listOf("volume", "up"),
                          listOf("volume", "down"),
                          listOf("brightness", "up"),
                          listOf("brightness", "down"),
                          listOf("open", "browser"),
                          listOf("open", "calendar"),
                          listOf("music", "play"),
                          listOf("music", "pause"),
                          listOf("music", "next"),
                          listOf("stop", "service"))
        }

    private val functions: List<() -> Unit>
        get() {
            return listOf({ toggleBluetooth(true) },
                          { toggleBluetooth(false) },
                          { toggleWiFi(true) },
                          { toggleWiFi(false) },
                          { toggleFlashLight(true) },
                          { toggleFlashLight(false) },
                          { callPhone("121") },
                          { callPhone("100") },
                          { changeAudio(AudioManager.ADJUST_RAISE) },
                          { changeAudio(AudioManager.ADJUST_LOWER) },
                          { changeBrightness(true) },
                          { changeBrightness(false) },
                          { openApp("com.android.chrome") },
                          { openApp("com.google.android.calendar") },
                          { handleMusic("play") },
                          { handleMusic("pause") },
                          { handleMusic("next") },
                          { stopService() })
        }

    fun perform(action: List<String>): Boolean {
        val functionIndex = actions.indexOf(action)
        if (functionIndex != -1) {
            functions[functionIndex]()
            return true
        }
        if (action.size >= 2) {
            return true
        }
        return false
    }

    private fun toggleBluetooth(enable: Boolean) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (enable) {
            bluetoothAdapter.enable()
        } else {
            bluetoothAdapter.disable()
        }
    }

    private fun changeAudio(mode: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustVolume(mode, AudioManager.FLAG_PLAY_SOUND)
    }

    private fun changeBrightness(mode: Boolean) {
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
        val brightness = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        val newBrightness = max(0, min(brightness + if (mode) 50 else -50, 255))
        Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, newBrightness)
    }

    private fun openApp(code: String) {
        context.startActivity(context.packageManager.getLaunchIntentForPackage(code).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @SuppressLint("MissingPermission")
    private fun callPhone(phone: String) {
        context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone")).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun handleMusic(mode: String) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (mode) {
            "play" -> {
                val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY)
                audioManager.dispatchMediaKeyEvent(event)
            }
            "pause" -> {
                val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
                audioManager.dispatchMediaKeyEvent(event)
            }
            "next" -> {
                val event = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT)
                audioManager.dispatchMediaKeyEvent(event)
            }
        }
    }

    private fun toggleFlashLight(enable: Boolean) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraManager.setTorchMode(cameraManager.cameraIdList[0], enable)
    }

    private fun toggleWiFi(enable: Boolean) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = enable
    }

    private fun stopService() {
        context.stopService(Intent(context, FloatingService::class.java))
    }
}