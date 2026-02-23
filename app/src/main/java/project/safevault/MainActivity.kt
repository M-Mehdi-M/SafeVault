package project.safevault

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import project.safevault.ui.auth.AuthActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, AuthActivity::class.java))
        finish()
    }
}