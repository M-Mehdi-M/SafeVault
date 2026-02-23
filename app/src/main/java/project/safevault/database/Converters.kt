package project.safevault.database

import androidx.room.TypeConverter
import project.safevault.models.ItemCategory

class Converters {

    @TypeConverter
    fun fromCategory(category: ItemCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): ItemCategory {
        return ItemCategory.valueOf(value)
    }
}

