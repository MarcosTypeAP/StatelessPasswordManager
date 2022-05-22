package com.sombromar.passwdgen

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.math.BigInteger
import java.security.MessageDigest

class UserAuthentication(private val activity: MainActivity) {

    private val passwdHash = EnvironmentVariables.PASSWORD_HASH
    private var passwdFails = 0

    private fun getSHA512(input: String): String {
        val md: MessageDigest = MessageDigest.getInstance("SHA-512")
        val messageDigest = md.digest(input.toByteArray())
        val no = BigInteger(1, messageDigest)
        var hashText: String = no.toString(16)
        while (hashText.length < 32) {
            hashText = "0$hashText"
        }
        return hashText
    }

    fun authenticateUser(passwd: String): Boolean {
        if (getSHA512(passwd) != this.passwdHash) {
            if (this.passwdFails < 5) {
                this.passwdFails++
                activity.vibratePhone()
                return false
            } else {
                activity.finish()
            }
        }
        this.passwdFails = 0
        return true
    }

    fun requestFingerprint(callback: (() -> Unit)? = null) {
        var authFails = 0
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    activity.finish()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    callback?.invoke()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    authFails++
                    if (authFails >= 3) {
                        activity.finish()
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for PasswdGen")
            .setSubtitle("Log in using your biometric credential")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .setNegativeButtonText("Exit")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

}