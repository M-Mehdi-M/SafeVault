package project.safevault.models

data class ScannedDocument(
    val rawText: String,
    val extractedFields: Map<String, String>
)

