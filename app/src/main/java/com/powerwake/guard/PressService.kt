package com.powerwake.guard

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

/**
 * Android does not let an app intercept the power button itself, so this works from the side:
 *
 *  - every time the screen turns ON, we count it as one "press"
 *  - if the sequence has not reached the chosen number of presses yet, we immediately lock the
 *    screen again (so it looks like the phone is switched off)
 *  - once the required number of presses happens in a row, we leave the screen on
 *
 * Locking uses the accessibility "lock screen" action (Android 9+), which behaves like a normal
 * power-button lock, so fingerprint / face unlock keeps working afterwards.
 */
class PressService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var count = 0
    private var lastScreenOn = 0L

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_ON) {
                onScreenOn()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    private fun onScreenOn() {
        val prefs = Prefs(this)
        if (!prefs.enabled) {
            count = 0
            return
        }

        // Never interfere with a ringing or ongoing call.
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mode = audio.mode
        if (mode == AudioManager.MODE_RINGTONE ||
            mode == AudioManager.MODE_IN_CALL ||
            mode == AudioManager.MODE_IN_COMMUNICATION
        ) {
            count = 0
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (now - lastScreenOn > prefs.gapSeconds * 1000L) {
            count = 0 // too slow, start a new sequence
        }
        lastScreenOn = now
        count++

        if (count >= prefs.presses) {
            // Correct sequence: leave the screen on and reset for next time.
            count = 0
            return
        }

        // Not enough presses yet: put the screen straight back to sleep.
        handler.post {
            val power = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (power.isInteractive) {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used. The service only exists so Android lets us lock the screen.
    }

    override fun onInterrupt() {
        // Not used.
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered.
        }
        super.onDestroy()
    }
}
