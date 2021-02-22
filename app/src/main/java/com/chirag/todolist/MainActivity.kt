package com.chirag.todolist

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUp()
    }
    fun setUp() {
        var spannableString =
            SpannableString(
                String.format("ToDo " + "List")
            )
        // applying color styles to substring
        val color = ForegroundColorSpan(Color.parseColor("#FF8F00"))
        spannableString.setSpan(color, 4, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView?.setText(spannableString)
        // To start Splash in Animation
        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity
            startActivity(Intent(this, DashBoardActivity::class.java))
            // close this activity
            finish()
        }, 3000)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            startActivity(Intent(this, DashBoardActivity::class.java))
        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
            startActivity(Intent(this, DashBoardActivity::class.java))
        }
    }
}