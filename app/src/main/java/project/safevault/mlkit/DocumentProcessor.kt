package project.safevault.mlkit

import project.safevault.models.ScannedDocument

object DocumentProcessor {

    private val namePattern = Regex("(?i)(?:name|nom)[:\\s]*([A-Za-z\\s]+)")
    private val idPattern = Regex("(?i)(?:id|no|number|#)[:\\s]*(\\w[\\w\\-]+)")
    private val datePattern = Regex("(\\d{1,2}[/\\-.]\\d{1,2}[/\\-.]\\d{2,4})")
    private val amountPattern = Regex("(?i)(?:total|amount|sum)[:\\s]*[\\$€£]?\\s*(\\d+[.,]\\d{2})")
    private val emailPattern = Regex("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}")
    private val phonePattern = Regex("(?:\\+?\\d{1,3}[\\s.-]?)?\\(?\\d{2,4}\\)?[\\s.-]?\\d{3,4}[\\s.-]?\\d{3,4}")

    fun process(rawText: String): ScannedDocument {
        val fields = mutableMapOf<String, String>()
        namePattern.find(rawText)?.groupValues?.getOrNull(1)?.trim()?.let {
            if (it.isNotBlank()) fields["Name"] = it
        }
        idPattern.find(rawText)?.groupValues?.getOrNull(1)?.trim()?.let {
            if (it.isNotBlank()) fields["ID Number"] = it
        }
        datePattern.findAll(rawText).firstOrNull()?.value?.let {
            fields["Date"] = it
        }
        amountPattern.find(rawText)?.groupValues?.getOrNull(1)?.let {
            fields["Amount"] = it
        }
        emailPattern.find(rawText)?.value?.let {
            fields["Email"] = it
        }
        phonePattern.find(rawText)?.value?.let {
            fields["Phone"] = it
        }
        return ScannedDocument(rawText, fields)
    }
}

