package project.safevault.security

import android.util.Base64
import java.nio.ByteBuffer

data class EncryptedPayload(val iv: ByteArray, val ciphertext: ByteArray) {

    fun toBase64(): String {
        val buffer = ByteBuffer.allocate(4 + iv.size + ciphertext.size)
        buffer.putInt(iv.size)
        buffer.put(iv)
        buffer.put(ciphertext)
        return Base64.encodeToString(buffer.array(), Base64.NO_WRAP)
    }

    companion object {
        fun fromBase64(encoded: String): EncryptedPayload {
            val data = Base64.decode(encoded, Base64.NO_WRAP)
            val buffer = ByteBuffer.wrap(data)
            val ivLength = buffer.int
            val iv = ByteArray(ivLength)
            buffer.get(iv)
            val ciphertext = ByteArray(buffer.remaining())
            buffer.get(ciphertext)
            return EncryptedPayload(iv, ciphertext)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptedPayload) return false
        return iv.contentEquals(other.iv) && ciphertext.contentEquals(other.ciphertext)
    }

    override fun hashCode(): Int {
        return 31 * iv.contentHashCode() + ciphertext.contentHashCode()
    }
}

object CryptoUtils {

    fun encrypt(plainText: String): String {
        val cipher = KeystoreManager.getEncryptCipher()
        val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val payload = EncryptedPayload(cipher.iv, ciphertext)
        return payload.toBase64()
    }

    fun decrypt(encryptedBase64: String): String {
        val payload = EncryptedPayload.fromBase64(encryptedBase64)
        val cipher = KeystoreManager.getDecryptCipher(payload.iv)
        val plainBytes = cipher.doFinal(payload.ciphertext)
        return String(plainBytes, Charsets.UTF_8)
    }

    fun encryptBytes(data: ByteArray): EncryptedPayload {
        val cipher = KeystoreManager.getEncryptCipher()
        val ciphertext = cipher.doFinal(data)
        return EncryptedPayload(cipher.iv, ciphertext)
    }

    fun decryptBytes(payload: EncryptedPayload): ByteArray {
        val cipher = KeystoreManager.getDecryptCipher(payload.iv)
        return cipher.doFinal(payload.ciphertext)
    }
}

