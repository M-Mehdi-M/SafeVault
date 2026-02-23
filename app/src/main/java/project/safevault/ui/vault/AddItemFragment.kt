package project.safevault.ui.vault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import project.safevault.R
import project.safevault.databinding.FragmentAddItemBinding
import project.safevault.models.ItemCategory

class AddItemFragment : Fragment() {

    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VaultViewModel by activityViewModels()
    private var editItemId: Long = -1L
    private var scannedImagePath: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryDropdown()
        val scannedText = arguments?.getString("scannedText") ?: ""
        if (scannedText.isNotBlank()) {
            binding.etContent.setText(scannedText)
        }
        scannedImagePath = arguments?.getString("scannedImagePath")
        editItemId = arguments?.getLong("editItemId", -1L) ?: -1L
        if (editItemId != -1L) {
            loadItemForEdit(editItemId)
        }
        binding.btnSave.setOnClickListener { saveItem() }
        binding.btnAttachScan.setOnClickListener {
            findNavController().navigate(R.id.scannerFragment)
        }
    }

    private fun setupCategoryDropdown() {
        val categories = ItemCategory.entries.map { it.displayName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
        binding.actvCategory.setText(ItemCategory.NOTE.displayName, false)
    }

    private fun loadItemForEdit(id: Long) {
        viewModel.loadItem(id)
        viewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            item?.let {
                binding.etTitle.setText(it.title)
                binding.actvCategory.setText(it.category.displayName, false)
            }
        }
        viewModel.decryptedContent.observe(viewLifecycleOwner) { content ->
            binding.etContent.setText(content)
        }
        binding.btnSave.text = getString(R.string.add_save)
    }

    private fun saveItem() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val categoryName = binding.actvCategory.text.toString()
        if (title.isBlank() || content.isBlank()) {
            Toast.makeText(requireContext(), R.string.add_error_empty, Toast.LENGTH_SHORT).show()
            return
        }
        val category = ItemCategory.fromDisplayName(categoryName)
        if (editItemId != -1L) {
            viewModel.selectedItem.value?.let { existing ->
                viewModel.updateItem(existing, title, content, category)
            }
        } else {
            viewModel.addItem(title, content, category, scannedImagePath)
        }
        Toast.makeText(requireContext(), R.string.add_saved_success, Toast.LENGTH_SHORT).show()
        findNavController().popBackStack(R.id.vaultListFragment, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

