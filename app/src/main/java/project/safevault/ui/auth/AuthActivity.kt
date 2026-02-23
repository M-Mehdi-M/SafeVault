package project.safevault.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import project.safevault.R
import project.safevault.databinding.ActivityAuthBinding
import project.safevault.database.SafeVaultDatabase
import project.safevault.security.BiometricHelper
import project.safevault.security.StealthManager
import project.safevault.ui.stealth.StealthActivity
import project.safevault.ui.vault.VaultActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private var isSetupMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isSetupMode = StealthManager.isFirstLaunch(this) || !StealthManager.hasUserPassword(this)
        if (isSetupMode) {
            setupFirstLaunch()
        } else {
            setupReturningUser()
        }
        binding.btnUnlock.setOnClickListener {
            handlePasswordUnlock()
        }
        animateEntrance()
    }

    private fun setupFirstLaunch() {
        binding.tvSubtitle.text = "Create a password to get started"
        binding.tvFingerprintHint.text = "Set up your master password"
        binding.ivFingerprint.visibility = View.GONE
        binding.tilPassword.hint = "Create Master Password"
        binding.btnUnlock.text = "Set Up"
    }

    private fun setupReturningUser() {
        if (BiometricHelper.canAuthenticate(this)) {
            binding.ivFingerprint.visibility = View.VISIBLE
            binding.tvFingerprintHint.visibility = View.VISIBLE
            triggerBiometric()
            binding.ivFingerprint.setOnClickListener { triggerBiometric() }
        } else {
            binding.ivFingerprint.visibility = View.GONE
            binding.tvFingerprintHint.text = getString(R.string.auth_no_biometric)
        }
    }

    private fun triggerBiometric() {
        BiometricHelper.authenticate(
            activity = this,
            title = getString(R.string.auth_biometric_title),
            subtitle = getString(R.string.auth_biometric_subtitle),
            negativeButtonText = getString(R.string.auth_biometric_negative),
            onSuccess = { navigateToVault() },
            onFailure = { error -> showError(error) }
        )
    }

    private fun handlePasswordUnlock() {
        val password = binding.etPassword.text.toString().trim()
        if (password.isEmpty()) {
            showError("Password cannot be empty")
            return
        }
        if (isSetupMode) {
            if (password.length < 4) {
                showError("Password must be at least 4 characters")
                return
            }
            StealthManager.setUserPassword(this, password)
            navigateToVault()
            return
        }
        if (StealthManager.isPanicPassword(this, password)) {
            handlePanicMode()
            return
        }
        if (StealthManager.verifyUserPassword(this, password)) {
            navigateToVault()
        } else {
            showError(getString(R.string.auth_failed))
            binding.etPassword.text?.clear()
        }
    }

    private fun handlePanicMode() {
        if (StealthManager.isAutoDestructEnabled(this)) {
            CoroutineScope(Dispatchers.IO).launch {
                SafeVaultDatabase.getInstance(this@AuthActivity).vaultDao().deleteAllItems()
            }
            Toast.makeText(this, "Data wiped", Toast.LENGTH_SHORT).show()
        }
        startActivity(Intent(this, StealthActivity::class.java))
        finish()
    }

    private fun navigateToVault() {
        startActivity(Intent(this, VaultActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }

    private fun animateEntrance() {
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
            duration = 800
        }
        binding.ivLock.startAnimation(fadeIn)
        binding.tvTitle.startAnimation(fadeIn)
        binding.tvSubtitle.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in).apply {
            duration = 1000
            startOffset = 300
        })
    }
}

