package com.tudorluca.lantern.utils

import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.Cipher

/**
 * Created by Tudor Luca on 15/09/14.
 */
fun encrypt(message: String, cipher: Cipher): String {
    val messageBytes = message.getBytes("UTF8")
    val rawEncryptedBytes = cipher.doFinal(messageBytes)!!
    val encodedEncryptedBytes = android.util.Base64.encode(rawEncryptedBytes, android.util.Base64.DEFAULT)!!
    return String(encodedEncryptedBytes, "UTF8")
}

fun decrypt(encrypted: String, cipher: Cipher): String {
    val encryptedBytes = android.util.Base64.decode(encrypted, android.util.Base64.DEFAULT)
    val decryptedBytes = cipher.doFinal(encryptedBytes!!) as ByteArray
    return String(decryptedBytes, "UTF8")
}

fun initCipher(key: String, iv: String, mode: Int = Cipher.ENCRYPT_MODE): Cipher {
    val keySpec = SecretKeySpec(key.getBytes("UTF8"), "DES");
    val ivSpec = IvParameterSpec(iv.getBytes("UTF8"));
    val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding") as Cipher
    cipher.init(mode, keySpec, ivSpec)
    return cipher
}

fun fromX(encrypted: String, key: String): String {
    val decoded = android.util.Base64.decode(encrypted, android.util.Base64.DEFAULT)!!
    return x(String(decoded), key)
}

fun toX(message: String, key: String): String {
    val encoded = android.util.Base64.encode(x(message, key).getBytes("UTF8"), android.util.Base64.DEFAULT)!!
    return String(encoded)
}

fun x(message: String, key: String): String {
    val m = message.toCharArray()
    val k = key.toCharArray()
    val ml = m.size
    val kl = k.size
    val res = CharArray(size = ml)

    for (index in m.indices) {
        res[index] = (m[index].toInt() xor k[index % kl].toInt()).toChar()
    }

    return String(res)
}

val publicKey = ""
