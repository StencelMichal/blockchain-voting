package com.stencel.evoting.sealer

import java.security.*
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor
import kotlin.math.abs

class CryptographyUtils {

    enum class Cryptosystem {
        RSA;
    }

    companion object {

        private val secureRandom = SecureRandom()

        fun generateRsaKeyPair(cryptosystem: Cryptosystem = Cryptosystem.RSA): KeyPair {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048, secureRandom)
            return keyPairGenerator.generateKeyPair()
        }

        fun hash(value: String): ByteArray {
            val digest = MessageDigest.getInstance("SHA-256")
            return digest.digest(value.toByteArray())
        }

        fun encrypt(message: ByteArray, publicKey: PublicKey): ByteArray {
            val cipher = Cipher.getInstance("RSA/None/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            return cipher.doFinal(message)
        }

        fun xorByteArrays(arr1: ByteArray, arr2: ByteArray): ByteArray {
            val (equalSizeArr1, equalSizeArr2) = padToEqualSize(arr1, arr2)
            val result = ByteArray(equalSizeArr1.size)
            for (i in equalSizeArr1.indices) {
                result[i] = equalSizeArr1[i] xor equalSizeArr2[i]
            }
            return result
        }

        fun calculateHMac(data: ByteArray, key: ByteArray): ByteArray {
            val algorithm = "HmacSHA256"
            val hmacSha256 = Mac.getInstance(algorithm)
            hmacSha256.init(SecretKeySpec(key, algorithm))
            return hmacSha256.doFinal(data)
        }


        private fun padToEqualSize(arr1: ByteArray, arr2: ByteArray): Pair<ByteArray, ByteArray> {
            val difference = abs(arr1.size - arr2.size)
            return if (difference > 0) {
                val smallerArray = if (arr1.size < arr2.size) arr1 else arr2
                val otherArray = if (arr1.size < arr2.size) arr2 else arr1
                val padding = ByteArray(arr1.size) { 0 }
                val paddedArray = padding + smallerArray
                Pair(paddedArray, otherArray)
            } else {
                Pair(arr1, arr2)
            }
        }


    }
}