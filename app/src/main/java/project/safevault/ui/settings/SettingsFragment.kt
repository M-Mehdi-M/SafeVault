package project.safevault.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import project.safevault.R
import project.safevault.databinding.FragmentSettingsBinding
import project.safevault.security.StealthManager
import project.safevault.ui.vault.VaultViewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VaultViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.switchAutoDestruct.isChecked = StealthManager.isAutoDestructEnabled(requireContext())
        binding.switchAutoDestruct.setOnCheckedChangeListener { _, isChecked ->
            StealthManager.setAutoDestructEnabled(requireContext(), isChecked)
        }
        binding.layoutPanicPassword.setOnClickListener { showPanicPasswordDialog() }
        binding.layoutWipeAll.setOnClickListener { showWipeConfirmDialog() }
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            binding.tvVersion.text = getString(R.string.settings_version, packageInfo.versionName)
        } catch (_: Exception) {
            binding.tvVersion.text = getString(R.string.settings_version, "1.0")
        }
    }

    private fun showPanicPasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(
            android.R.layout.simple_list_item_1, null
        )
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(64, 32, 64, 0)
        }
        val tilPassword = TextInputLayout(requireContext()).apply {
            hint = getString(R.string.settings_panic_hint)
        }
        val etPassword = TextInputEditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        tilPassword.addView(etPassword)
        val tilConfirm = TextInputLayout(requireContext()).apply {
            hint = getString(R.string.settings_panic_confirm_hint)
        }
        val etConfirm = TextInputEditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        tilConfirm.addView(etConfirm)
        layout.addView(tilPassword)
        layout.addView(tilConfirm)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_panic_password)
            .setView(layout)
            .setPositiveButton(R.string.settings_save) { _, _ ->
                val password = etPassword.text.toString()
                val confirm = etConfirm.text.toString()
                when {
                    password.isBlank() -> Toast.makeText(requireContext(), R.string.settings_panic_empty, Toast.LENGTH_SHORT).show()
                    password != confirm -> Toast.makeText(requireContext(), R.string.settings_panic_mismatch, Toast.LENGTH_SHORT).show()
                    else -> {
                        StealthManager.setPanicPassword(requireContext(), password)
                        Toast.makeText(requireContext(), R.string.settings_panic_saved, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(R.string.settings_cancel, null)
            .show()
    }

    private fun showWipeConfirmDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_wipe_title)
            .setMessage(R.string.settings_wipe_confirm)
            .setPositiveButton(R.string.settings_wipe_all) { _, _ ->
                viewModel.deleteAllItems()
                Toast.makeText(requireContext(), R.string.settings_wipe_done, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.settings_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

