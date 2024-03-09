package com.stencel.evoting.sealer

import com.stencel.evoting.sealer.CryptographyUtils.Companion.cryptoHash
import com.stencel.evoting.sealer.CryptographyUtils.Companion.encrypt
import com.stencel.evoting.sealer.CryptographyUtils.Companion.keyedHash
import java.math.BigInteger
import java.security.*
import javax.crypto.Cipher


class RingSignatureCreator {

    companion object {

        private val random = SecureRandom()

        fun sign(message: String, signerKeyPair: KeyPair, publicKeys: List<PublicKey>): RingSignature {
            val ringSize = publicKeys.size + 1
            val hashedMessage = cryptoHash(message)

            val startGlueValue = BigInteger(1024, random)
            val glueValues = mutableListOf<BigInteger>(keyedHash(hashedMessage, startGlueValue.toString()))

            val userValues = mutableListOf<BigInteger>().apply {
                addAll((1 until ringSize).map { BigInteger(1024, random) })
                add(BigInteger.ZERO)
            }

            for (i in 0 until ringSize - 1) {
                val encryptedValue = encrypt(userValues[i], publicKeys[i])
                val xored = glueValues.last() xor encryptedValue
                println("glueValue: ${glueValues.last()}")
                glueValues += keyedHash(hashedMessage, xored.toString())
            }
            println("glueValue: ${glueValues.last()}")
            userValues[userValues.size - 1] = rsaDecrypt(glueValues.last() xor startGlueValue, signerKeyPair.private)

            val signatureRows = mutableListOf<Map<String, Any>>().apply {
                addAll((0 until ringSize -1).map { i ->
                    mapOf(
                        "e" to (publicKeys[i] as java.security.interfaces.RSAPublicKey).publicExponent,
                        "n" to (publicKeys[i] as java.security.interfaces.RSAPublicKey).modulus,
                        "userValues" to userValues[i]
                    )
                })
                add(
                    mapOf(
                        "e" to (signerKeyPair.public as java.security.interfaces.RSAPublicKey).publicExponent,
                        "n" to (signerKeyPair.public as java.security.interfaces.RSAPublicKey).modulus,
                        "userValues" to userValues.last()
                    )
                )
            }

            val rotation = 0
            val rotatedV = glueValues.takeLast(rotation) + glueValues.dropLast(rotation)
            val rotatedRows = signatureRows.takeLast(rotation) + signatureRows.dropLast(rotation)

            val x = mapOf(
                "msg" to message,
                "rows" to rotatedRows,
                "v" to rotatedV.last()
            )

            val y = RingSignature(
                keys = rotateList(publicKeys + signerKeyPair.public, rotation),
                startValue = rotatedV.first(),
                ringValues = rotatedRows.map { it["userValues"] as BigInteger }
            )

            return y
        }

        private fun <T> rotateList(list: List<T>, rotation: Int): List<T> {
            return list.takeLast(rotation) + list.dropLast(rotation)
        }


        fun rsaDecrypt(msg: BigInteger, privateKey: PrivateKey): BigInteger {
            val dec = Cipher.getInstance("RSA/ECB/NoPadding")
            dec.init(Cipher.DECRYPT_MODE, privateKey)
            val decryptedContentKey = dec.doFinal(msg.toByteArray())
            return BigInteger(1, decryptedContentKey)
        }
    }

}

