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
    fun perform(action: List<String>): Boolean {
        if (action.size > 2) {
            return false
        }
        when (action) {
            listOf("bluetooth", "on") -> toggleBluetooth(true)
            listOf("bluetooth", "off") -> toggleBluetooth(false)
            listOf("wifi", "on") -> toggleWiFi(true)
            listOf("wifi", "off") -> toggleWiFi(false)
            listOf("torch", "on") -> toggleFlashLight(true)
            listOf("torch", "off") -> toggleFlashLight(false)
            listOf("call", "home") -> callPhone("121")
            listOf("call", "emergency") -> callPhone("100")
            listOf("volume", "up") -> changeAudio(AudioManager.ADJUST_RAISE)
            listOf("volume", "down") -> changeAudio(AudioManager.ADJUST_LOWER)
            listOf("brightness", "up") -> changeBrightness(true)
            listOf("brightness", "down") -> changeBrightness(false)
            listOf("open", "browser") -> openApp("com.android.chrome")
            listOf("open", "calendar") -> openApp("com.google.android.calendar")
            listOf("music", "play") -> handleMusic("play")
            listOf("music", "pause") -> handleMusic("pause")
            listOf("music", "next") -> handleMusic("next")
            listOf("stop", "service") -> stopService()
            else -> return false
        }
        return true
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
        context.startActivity(context.packageManager.getLaunchIntentForPackage(code))
    }

    @SuppressLint("MissingPermission")
    private fun callPhone(phone: String) {
        context.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone")))
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