package com.sombromar.passwdgen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.*
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


private lateinit var mainActivity: MainActivity

class MainActivity : AppCompatActivity() {

    private val passwdGen = PasswordGenerator()
    private val userAuthentication = UserAuthentication(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainActivity = this

        main()
    }

    private fun main() {

        this.userAuthentication.requestFingerprint()

        passwordTextInput.setOnKeyListener(View.OnKeyListener { view, key, event ->
            if (key == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                handleInput(view)
                return@OnKeyListener true
            }
            false
        })

        showPasswordButton.setOnClickListener {
            vibratePhone()
            this.userAuthentication.requestFingerprint {
                generatedPasswordTextView.text = this.passwdGen.generatedPasswd
            }
        }

        copyPasswordToClipboardButton.setOnClickListener {
            vibratePhone()
            this.userAuthentication.requestFingerprint {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("", this.passwdGen.generatedPasswd)
                clipboard.setPrimaryClip(clipData)
            }
        }
    }

    private fun handleInput(view: View) {
        val appName = appNameTextInput.text.toString()
        if (appName == "") {
            appNameTextInput.requestFocus()
            return
        } else if (appName.length < 3) {
            vibratePhone()
            return
        }
        val isAuthenticated = this.userAuthentication.authenticateUser(
            passwordTextInput.text.toString()
        )
        if (!isAuthenticated) return
        hideKeyboard(view)
        passwordTextInput.clearFocus()
        this.passwdGen.genPassword(appName)
        generatedPasswordTextView.visibility = View.VISIBLE
        buttonsLinearLayout.visibility = View.VISIBLE
        passwordTextInput.setText("")
        appNameTextInput.setText("")
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun vibratePhone(long: Long = 50, amp: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
        if (Build.VERSION.SDK_INT >= 31) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            val effect = VibrationEffect.createOneShot(long, amp)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}