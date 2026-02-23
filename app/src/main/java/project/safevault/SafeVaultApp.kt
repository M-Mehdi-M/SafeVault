package project.safevault

import android.app.Application
import project.safevault.database.SafeVaultDatabase

class SafeVaultApp : Application() {

    val database: SafeVaultDatabase by lazy {
        SafeVaultDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SafeVaultApp
            private set
    }
}

