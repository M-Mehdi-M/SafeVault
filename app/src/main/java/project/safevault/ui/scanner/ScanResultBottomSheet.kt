package project.safevault.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import project.safevault.databinding.BottomSheetScanResultBinding

class ScanResultBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetScanResultBinding? = null
    private val binding get() = _binding!!
    var onSaveClicked: ((String) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetScanResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val text = arguments?.getString(ARG_TEXT) ?: ""
        binding.tvScanResult.text = text
        binding.btnSaveToVault.setOnClickListener {
            onSaveClicked?.invoke(text)
            dismiss()
        }
        binding.btnDiscard.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TEXT = "scan_text"

        fun newInstance(text: String): ScanResultBottomSheet {
            return ScanResultBottomSheet().apply {
                arguments = Bundle().apply { putString(ARG_TEXT, text) }
            }
        }
    }
}

