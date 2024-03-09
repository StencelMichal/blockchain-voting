package com.stencel.evoting.sealer

import com.stencel.evoting.sealer.CryptographyUtils.Companion.cryptoHash
import com.stencel.evoting.sealer.CryptographyUtils.Companion.encrypt
import com.stencel.evoting.sealer.CryptographyUtils.Companion.keyedHash
import java.math.BigInteger
import java.security.*
import javax.crypto.Cipher


class RingSignatureCreator {

    companion object {


        fun sign(message: String, signerKeyPair: KeyPair, publicKeys: List<PublicKey>): RingSignature {
            val ringSize = publicKeys.size + 1
            val hashedMessage = cryptoHash(message)
            val startGlueValue = BigInteger(1024, SecureRandom())
            val v = MutableList<BigInteger>(ringSize) { BigInteger.ZERO }
            v[0] = keyedHash(hashedMessage, startGlueValue.toString())

            val userValues = mutableListOf<BigInteger>().apply {
                add(BigInteger.ZERO)
                addAll((1 until ringSize).map { BigInteger(1024, SecureRandom()) })
            }

            for (i in 1 until ringSize) {
                val encryptedValue = encrypt(userValues[i], publicKeys[i - 1])
                val xored = v[i - 1] xor encryptedValue
                println("glueValue: ${v[i - 1]}")
                v[i] = keyedHash(hashedMessage, xored.toString())
            }
            println("glueValue: ${v[ringSize - 1]}")
            userValues[0] = rsaDecrypt(v[ringSize - 1] xor startGlueValue, signerKeyPair.private)

            val signatureRows = mutableListOf<Map<String, Any>>().apply {
                add(
                    mapOf(
                        "e" to (signerKeyPair.public as java.security.interfaces.RSAPublicKey).publicExponent,
                        "n" to (signerKeyPair.public as java.security.interfaces.RSAPublicKey).modulus,
                        "userValues" to userValues[0]
                    )
                )
                addAll((1 until ringSize).map { i ->
                    mapOf(
                        "e" to (publicKeys[i - 1] as java.security.interfaces.RSAPublicKey).publicExponent,
                        "n" to (publicKeys[i - 1] as java.security.interfaces.RSAPublicKey).modulus,
                        "userValues" to userValues[i]
                    )
                })
            }

            val rotation = 0
            val rotatedV = v.takeLast(rotation) + v.dropLast(rotation)
            val rotatedRows = signatureRows.takeLast(rotation) + signatureRows.dropLast(rotation)

            val x = mapOf(
                "msg" to message,
                "rows" to rotatedRows,
                "v" to rotatedV.last()
            )

            val y = RingSignature(
                keys = rotateList(listOf(signerKeyPair.public) + publicKeys, rotation),
                startValue = rotatedV.last(),
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

