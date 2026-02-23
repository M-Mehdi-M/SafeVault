package project.safevault.ui.vault

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import project.safevault.R
import project.safevault.databinding.ActivityVaultBinding

class VaultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVaultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVaultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.coordinatorLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.vaultListFragment, R.id.scannerFragment, R.id.settingsFragment)
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.vaultListFragment -> binding.toolbar.title = getString(R.string.app_name)
                R.id.scannerFragment -> binding.toolbar.title = getString(R.string.scanner_title)
                R.id.settingsFragment -> binding.toolbar.title = getString(R.string.settings_title)
                R.id.addItemFragment -> binding.toolbar.title = getString(R.string.add_item_title)
                R.id.vaultDetailFragment -> binding.toolbar.title = ""
            }
        }
    }
}

