package com.stencel.evoting.sealer

import java.math.BigInteger
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

        fun rsaEncrypt(msg: BigInteger, publicKey: PublicKey): BigInteger {
            val enc = Cipher.getInstance("RSA/ECB/NoPadding")
            enc.init(Cipher.ENCRYPT_MODE, publicKey)
            val encryptedContentKey = enc.doFinal(truncateLeadingZeros(msg.toByteArray()))
            return BigInteger(1, encryptedContentKey)
        }

        fun rsaDecrypt(msg: BigInteger, privateKey: PrivateKey): BigInteger {
            val dec = Cipher.getInstance("RSA/ECB/NoPadding")
            dec.init(Cipher.DECRYPT_MODE, privateKey)
            val decryptedContentKey = dec.doFinal(msg.toByteArray())
            return BigInteger(1, decryptedContentKey)
        }


        fun cryptoHash(msg: String): BigInteger {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(msg.toByteArray())
            return BigInteger(1, hashBytes)
        }


        fun keyedHash(key: BigInteger, msg: String): BigInteger {
            val hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
            hmac.init(secretKey)
            val hashBytes = hmac.doFinal(msg.toByteArray())
            return BigInteger(1, hashBytes)
        }


        private fun truncateLeadingZeros(input: ByteArray): ByteArray {
            var startIndex = 0
            for (i in input.indices) {
                if (input[i] != 0.toByte()) {
                    startIndex = i
                    break
                }
            }
            return input.copyOfRange(startIndex, input.size)
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