package project.safevault.ui.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import project.safevault.R
import project.safevault.databinding.FragmentVaultDetailBinding
import project.safevault.models.ItemCategory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VaultDetailFragment : Fragment() {

    private var _binding: FragmentVaultDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VaultViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVaultDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemId = arguments?.getLong("itemId") ?: return
        viewModel.loadItem(itemId)
        viewModel.selectedItem.observe(viewLifecycleOwner) { item ->
            item?.let {
                binding.tvDetailTitle.text = it.title
                binding.tvDetailCategoryLabel.text = it.category.displayName
                val (iconRes, colorRes) = getCategoryVisuals(it.category)
                binding.ivDetailCategory.setImageResource(iconRes)
                val color = requireContext().getColor(colorRes)
                binding.ivDetailCategory.setColorFilter(color)
                binding.detailCategoryBg.background.setTint(color and 0x00FFFFFF or 0x1A000000)
                val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
                binding.tvCreatedAt.text = getString(R.string.detail_created, sdf.format(Date(it.createdAt)))
                binding.tvUpdatedAt.text = getString(R.string.detail_updated, sdf.format(Date(it.updatedAt)))

                val imagePath = it.scannedImagePath
                if (!imagePath.isNullOrBlank()) {
                    val file = File(imagePath)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            binding.ivScannedImage.setImageBitmap(bitmap)
                            binding.cardScannedImage.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
        viewModel.decryptedContent.observe(viewLifecycleOwner) { content ->
            binding.tvDetailContent.text = content
        }
        binding.btnCopy.setOnClickListener {
            val content = binding.tvDetailContent.text.toString()
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("SafeVault", content))
            Toast.makeText(requireContext(), R.string.detail_copied, Toast.LENGTH_SHORT).show()
        }
        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.detail_delete_title)
                .setMessage(R.string.detail_delete_confirm)
                .setPositiveButton(R.string.detail_delete) { _, _ ->
                    viewModel.selectedItem.value?.let { viewModel.deleteItem(it) }
                    findNavController().popBackStack()
                }
                .setNegativeButton(R.string.detail_cancel, null)
                .show()
        }
    }

    private fun getCategoryVisuals(category: ItemCategory): Pair<Int, Int> {
        return when (category) {
            ItemCategory.ID_CARD -> R.drawable.ic_id_card to R.color.card_id
            ItemCategory.PASSWORD -> R.drawable.ic_password to R.color.card_password
            ItemCategory.NOTE -> R.drawable.ic_note to R.color.card_note
            ItemCategory.RECEIPT -> R.drawable.ic_receipt to R.color.card_receipt
            ItemCategory.PHOTO -> R.drawable.ic_photo to R.color.card_photo
            ItemCategory.OTHER -> R.drawable.ic_other to R.color.card_other
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

