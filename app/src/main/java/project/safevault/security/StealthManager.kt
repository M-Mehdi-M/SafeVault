package project.safevault.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

object StealthManager {

    private const val PREFS_NAME = "safevault_secure_prefs"
    private const val KEY_PANIC_HASH = "panic_password_hash"
    private const val KEY_AUTO_DESTRUCT = "auto_destruct_enabled"
    private const val KEY_USER_PASSWORD_HASH = "user_password_hash"
    private const val KEY_FIRST_LAUNCH = "first_launch"

    private fun getPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun isFirstLaunch(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setUserPassword(context: Context, password: String) {
        getPrefs(context).edit()
            .putString(KEY_USER_PASSWORD_HASH, hashPassword(password))
            .putBoolean(KEY_FIRST_LAUNCH, false)
            .apply()
    }

    fun verifyUserPassword(context: Context, password: String): Boolean {
        val storedHash = getPrefs(context).getString(KEY_USER_PASSWORD_HASH, null) ?: return false
        return storedHash == hashPassword(password)
    }

    fun hasUserPassword(context: Context): Boolean {
        return getPrefs(context).getString(KEY_USER_PASSWORD_HASH, null) != null
    }

    fun setPanicPassword(context: Context, password: String) {
        getPrefs(context).edit().putString(KEY_PANIC_HASH, hashPassword(password)).apply()
    }

    fun isPanicPassword(context: Context, input: String): Boolean {
        val storedHash = getPrefs(context).getString(KEY_PANIC_HASH, null) ?: return false
        return storedHash == hashPassword(input)
    }

    fun hasPanicPassword(context: Context): Boolean {
        return getPrefs(context).getString(KEY_PANIC_HASH, null) != null
    }

    fun setAutoDestructEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_DESTRUCT, enabled).apply()
    }

    fun isAutoDestructEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_DESTRUCT, false)
    }
}

