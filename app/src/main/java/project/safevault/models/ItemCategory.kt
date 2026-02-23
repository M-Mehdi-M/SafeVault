package project.safevault.models

enum class ItemCategory(val displayName: String) {
    ID_CARD("ID Card"),
    PASSWORD("Password"),
    NOTE("Secure Note"),
    RECEIPT("Receipt"),
    PHOTO("Photo"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(name: String): ItemCategory {
            return entries.find { it.displayName == name } ?: OTHER
        }
    }
}

