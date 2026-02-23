package project.safevault.ui.vault

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import project.safevault.R
import project.safevault.database.VaultItemEntity
import project.safevault.databinding.ItemVaultBinding
import project.safevault.models.ItemCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VaultItemAdapter(
    private val onItemClick: (VaultItemEntity) -> Unit
) : ListAdapter<VaultItemEntity, VaultItemAdapter.VaultViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaultViewHolder {
        val binding = ItemVaultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VaultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VaultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VaultViewHolder(private val binding: ItemVaultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VaultItemEntity) {
            binding.tvItemTitle.text = item.title
            binding.tvItemCategory.text = item.category.displayName
            binding.tvItemDate.text = formatDate(item.updatedAt)
            val (iconRes, tintColor) = getCategoryVisuals(item.category)
            binding.ivCategoryIcon.setImageResource(iconRes)
            binding.ivCategoryIcon.setColorFilter(
                binding.root.context.getColor(tintColor)
            )
            binding.categoryIconBg.background.setTint(
                binding.root.context.getColor(tintColor) and 0x1AFFFFFF or 0x1A000000
            )
            binding.root.setOnClickListener { onItemClick(item) }
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

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<VaultItemEntity>() {
            override fun areItemsTheSame(oldItem: VaultItemEntity, newItem: VaultItemEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: VaultItemEntity, newItem: VaultItemEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}

