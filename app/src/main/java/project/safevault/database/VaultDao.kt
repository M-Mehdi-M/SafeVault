package project.safevault.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import project.safevault.models.ItemCategory

@Dao
interface VaultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItemEntity): Long

    @Update
    suspend fun updateItem(item: VaultItemEntity)

    @Delete
    suspend fun deleteItem(item: VaultItemEntity)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("SELECT * FROM vault_items WHERE id = :id")
    suspend fun getItemById(id: Long): VaultItemEntity?

    @Query("SELECT * FROM vault_items ORDER BY updatedAt DESC")
    fun getAllItems(): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE category = :category ORDER BY updatedAt DESC")
    fun getItemsByCategory(category: ItemCategory): Flow<List<VaultItemEntity>>

    @Query("SELECT * FROM vault_items WHERE title LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchItems(query: String): Flow<List<VaultItemEntity>>

    @Query("DELETE FROM vault_items")
    suspend fun deleteAllItems()

    @Query("SELECT COUNT(*) FROM vault_items")
    suspend fun getItemCount(): Int
}

