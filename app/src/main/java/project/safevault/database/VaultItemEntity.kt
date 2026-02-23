package project.safevault.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import project.safevault.models.ItemCategory

@Entity(tableName = "vault_items")
data class VaultItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val encryptedContent: String,
    val category: ItemCategory,
    val scannedImagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

