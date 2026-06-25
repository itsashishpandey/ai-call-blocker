package com.smartcallblocker.app.util

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

object BiometricGate {

    /** True if the device has biometrics or a device credential available. */
    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val mgr = BiometricManager.from(activity)
        val flags = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return mgr.canAuthenticate(flags) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Show the prompt. [onSuccess] runs on the UI thread once authentication passes.
     */
    fun show(
        activity: FragmentActivity,
        title: String = "Unlock AI Spam Call Blocker",
        subtitle: String = "Confirm it's you to continue",
        onSuccess: () -> Unit,
        onFailure: () -> Unit = {},
    ) {
        val executor = androidx.core.content.ContextCompat.getMainExecutor(activity)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
            .build()

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) =
                    onSuccess()

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onFailure()
                }
            },
        )
        prompt.authenticate(info)
    }
}
