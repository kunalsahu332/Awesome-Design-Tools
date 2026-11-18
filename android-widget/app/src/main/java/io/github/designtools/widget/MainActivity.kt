package io.github.designtools.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/** Minimal launcher screen: explains the widget and offers to pin it. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (24 * resources.displayMetrics.density).toInt()

        val intro = TextView(this).apply {
            setText(R.string.main_intro)
            textSize = 16f
            gravity = Gravity.CENTER
        }
        val pin = Button(this).apply {
            setText(R.string.main_pin)
            setOnClickListener { requestPin() }
        }
        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(pad, pad, pad, pad)
            addView(intro)
            addView(pin, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { topMargin = pad; gravity = Gravity.CENTER_HORIZONTAL })
        })
    }

    private fun requestPin() {
        val manager = getSystemService(AppWidgetManager::class.java)
        if (manager != null && manager.isRequestPinAppWidgetSupported) {
            manager.requestPinAppWidget(ComponentName(this, DesignToolWidgetReceiver::class.java), null, null)
        } else {
            Toast.makeText(this, R.string.main_pin_unsupported, Toast.LENGTH_LONG).show()
        }
    }
}
