package com.stencel.evoting.sealer

import com.stencel.evoting.sealer.CryptographyUtils.Companion.calculateHMac
import com.stencel.evoting.sealer.CryptographyUtils.Companion.encrypt
import com.stencel.evoting.sealer.CryptographyUtils.Companion.hash
import com.stencel.evoting.sealer.CryptographyUtils.Companion.xorByteArrays
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher


class RingSignatureCreatorOld {

    companion object {

        private val random = SecureRandom()

        fun createRingSignature(message: String, signerKeyPair: KeyPair, publicKeys: List<PublicKey>): RingSignature {
            val ringSize = publicKeys.size + 1
            val hashedMessage = hash(message)
            val startGlueValue = randomByteArray(32)
            var glueValue = startGlueValue
            val userValues = List(ringSize - 1) { randomByteArray(32) }
            userValues.zip(publicKeys) { userValue, key ->
                println("RING VALUE: ${Base64.getEncoder().encodeToString(glueValue)}")
                val encrypted = encrypt(userValue, key)
                val xored = xorByteArrays(glueValue, encrypted)
                glueValue = calculateHMac(xored, hashedMessage)
            }
            println("RING VALUE: ${Base64.getEncoder().encodeToString(glueValue)}")
            val encryptedSignerValue = xorByteArrays(startGlueValue, glueValue)
//            val paddedExpectedLastRingValue = padWithZeros(expectedLastRingValue, 2048 / 8 - expectedLastRingValue.size)
            val signerValue = decrypt(encryptedSignerValue, signerKeyPair.private)

            val enctyptedSignerValue = truncateLeadingZeros(encrypt(signerValue, signerKeyPair.public))
            require(encryptedSignerValue.contentEquals(enctyptedSignerValue)) { "Signer value is not equal to encrypted signer value" }
            val xored = xorByteArrays(glueValue, enctyptedSignerValue)
            val lastGlueValue = calculateHMac(xored, hashedMessage)
            println("RING VALUE: ${Base64.getEncoder().encodeToString(lastGlueValue)}")
            require(lastGlueValue.contentEquals(startGlueValue)) { "Last glue value is not equal to start glue value" }
            val ringValues = userValues + signerValue
            val shift = random.nextInt(ringSize)
            val allPublicKeys = publicKeys + signerKeyPair.public
            val shiftedPublicKeys = allPublicKeys.subList(shift, ringSize) + allPublicKeys.subList(0, shift)
            val shiftedUserValues = ringValues.subList(shift, ringSize) + ringValues.subList(0, shift)

            return RingSignature(
                keys = allPublicKeys,
                startValue = Base64.getEncoder().encodeToString(startGlueValue),
                ringValues = ringValues.map { v -> Base64.getEncoder().encodeToString(v) }
            )
        }

        fun truncateLeadingZeros(input: ByteArray): ByteArray {
            var startIndex = 0
            for (i in input.indices) {
                if (input[i] != 0.toByte()) {
                    startIndex = i
                    break
                }
            }
            return input.copyOfRange(startIndex, input.size)
        }


        private fun padWithZeros(byteArray: ByteArray, numberOfZeros: Int): ByteArray {
            val padding = ByteArray(numberOfZeros) { 0 }
            return padding + byteArray
        }

        private fun randomByteArray(size: Int): ByteArray {
            val byteArray = ByteArray(size)
            random.nextBytes(byteArray)
            return byteArray
        }

        fun byteArrayToHex(a: ByteArray): String {
            val sb = StringBuilder(a.size * 2)
            for (b in a) sb.append(String.format("%02x", b))
            return sb.toString()
        }


        private fun decrypt(cipherText: ByteArray, privateKey: PrivateKey): ByteArray {
            val cipher = Cipher.getInstance("RSA/None/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            return cipher.doFinal(cipherText)
        }

    }
}