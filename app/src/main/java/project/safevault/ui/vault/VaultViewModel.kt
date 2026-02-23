package project.safevault.ui.vault

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import project.safevault.SafeVaultApp
import project.safevault.database.VaultItemEntity
import project.safevault.models.ItemCategory
import project.safevault.security.CryptoUtils

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = (application as SafeVaultApp).database.vaultDao()

    private val filterCategory = MutableStateFlow<ItemCategory?>(null)
    private val searchQuery = MutableStateFlow("")

    val allItems: LiveData<List<VaultItemEntity>> = filterCategory.flatMapLatest { category ->
        searchQuery.flatMapLatest { query ->
            when {
                query.isNotBlank() -> dao.searchItems(query)
                category != null -> dao.getItemsByCategory(category)
                else -> dao.getAllItems()
            }
        }
    }.asLiveData()

    private val _selectedItem = MutableLiveData<VaultItemEntity?>()
    val selectedItem: LiveData<VaultItemEntity?> = _selectedItem

    private val _decryptedContent = MutableLiveData<String>()
    val decryptedContent: LiveData<String> = _decryptedContent

    fun setFilter(category: ItemCategory?) {
        filterCategory.value = category
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun addItem(title: String, content: String, category: ItemCategory, scannedImagePath: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val encrypted = CryptoUtils.encrypt(content)
            val entity = VaultItemEntity(
                title = title,
                encryptedContent = encrypted,
                category = category,
                scannedImagePath = scannedImagePath
            )
            dao.insertItem(entity)
        }
    }

    fun updateItem(item: VaultItemEntity, newTitle: String, newContent: String, newCategory: ItemCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            val encrypted = CryptoUtils.encrypt(newContent)
            val updated = item.copy(
                title = newTitle,
                encryptedContent = encrypted,
                category = newCategory,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateItem(updated)
        }
    }

    fun deleteItem(item: VaultItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteItem(item)
        }
    }

    fun loadItem(itemId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = dao.getItemById(itemId)
            _selectedItem.postValue(item)
            item?.let {
                try {
                    val decrypted = CryptoUtils.decrypt(it.encryptedContent)
                    _decryptedContent.postValue(decrypted)
                } catch (e: Exception) {
                    _decryptedContent.postValue("[Decryption failed]")
                }
            }
        }
    }

    fun decryptContent(encrypted: String): String {
        return try {
            CryptoUtils.decrypt(encrypted)
        } catch (e: Exception) {
            "[Decryption failed]"
        }
    }

    fun deleteAllItems() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteAllItems()
        }
    }
}

