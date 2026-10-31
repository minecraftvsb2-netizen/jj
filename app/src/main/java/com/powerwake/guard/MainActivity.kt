package com.powerwake.guard

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var prefs: Prefs
    private lateinit var statusText: TextView
    private lateinit var pressesText: TextView
    private lateinit var gapText: TextView
    private lateinit var summaryText: TextView

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(40), dp(20), dp(24))
        }

        root.addView(text("Power Wake Guard", 26f, bold = true))
        root.addView(
            text(
                "Your phone only stays awake after the right number of quick power-button " +
                    "presses. Fewer presses and the screen goes straight back to black.",
                15f
            ).apply { setPadding(0, dp(8), 0, dp(16)) }
        )

        // Step 1: accessibility service
        statusText = text("", 16f, bold = true)
        root.addView(statusText)
        root.addView(
            text(
                "Android 13+: if the switch is greyed out, open this app's App info, tap the " +
                    "three-dot menu and choose \"Allow restricted settings\" first.",
                13f
            ).apply { setPadding(0, dp(4), 0, dp(8)) }
        )
        root.addView(button("1. Turn on the service") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        root.addView(button("Open App info (restricted settings)") {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:$packageName"))
            )
        })
        root.addView(button("2. Allow background use (battery)") {
            startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        })

        // Master switch
        val master = Switch(this).apply {
            text = "Protection on"
            textSize = 17f
            isChecked = prefs.enabled
            setPadding(0, dp(16), 0, dp(8))
            setOnCheckedChangeListener { _, checked ->
                prefs.enabled = checked
                refresh()
            }
        }
        root.addView(master)

        // Presses stepper
        pressesText = text("", 17f)
        root.addView(stepper("Presses needed", pressesText,
            onMinus = { prefs.presses = prefs.presses - 1; refresh() },
            onPlus = { prefs.presses = prefs.presses + 1; refresh() }))

        // Gap stepper
        gapText = text("", 17f)
        root.addView(stepper("Max pause between presses", gapText,
            onMinus = { prefs.gapSeconds = prefs.gapSeconds - 1; refresh() },
            onPlus = { prefs.gapSeconds = prefs.gapSeconds + 1; refresh() }))

        summaryText = text("", 15f).apply { setPadding(0, dp(16), 0, dp(8)) }
        root.addView(summaryText)

        root.addView(
            text(
                "Tips: turn off Tap to wake, Lift to wake and wake-on-notification in your " +
                    "phone settings, and keep it on silent. The screen may light for a split " +
                    "second on each press; Android does not let apps block the power button.",
                13f
            )
        )

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val on = serviceEnabled()
        statusText.text = if (on) "Service: ON (working)" else "Service: OFF (turn it on below)"
        statusText.setTextColor(if (on) 0xFF2E7D32.toInt() else 0xFFC62828.toInt())

        pressesText.text = prefs.presses.toString()
        gapText.text = "${prefs.gapSeconds} s"

        summaryText.text = if (prefs.enabled) {
            "Press the power button ${prefs.presses} times, each within ${prefs.gapSeconds} s " +
                "of the last, to wake the phone. Anything less and it stays dark."
        } else {
            "Protection is off: the phone wakes normally."
        }
    }

    private fun serviceEnabled(): Boolean {
        val master = Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0)
        if (master != 1) return false
        val list = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val me = ComponentName(this, PressService::class.java)
        return list.split(':').any { ComponentName.unflattenFromString(it) == me }
    }

    private fun text(value: String, sizeSp: Float, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = sizeSp
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun button(label: String, onClick: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { onClick() }
        }

    private fun stepper(
        title: String,
        valueView: TextView,
        onMinus: () -> Unit,
        onPlus: () -> Unit
    ): View {
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(4))
        }
        column.addView(text(title, 14f))
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        row.addView(button("-") { onMinus() })
        valueView.apply {
            gravity = Gravity.CENTER
            minWidth = dp(72)
        }
        row.addView(valueView)
        row.addView(button("+") { onPlus() })
        column.addView(row)
        return column
    }
}
