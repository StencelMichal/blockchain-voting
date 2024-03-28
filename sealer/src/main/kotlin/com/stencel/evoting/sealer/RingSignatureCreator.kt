package com.stencel.evoting.sealer

import com.stencel.evoting.smartcontracts.CryptographyUtils.Companion.cryptoHash
import com.stencel.evoting.smartcontracts.CryptographyUtils.Companion.rsaEncrypt
import com.stencel.evoting.smartcontracts.CryptographyUtils.Companion.keyedHash
import com.stencel.evoting.smartcontracts.CryptographyUtils.Companion.rsaDecrypt
import java.math.BigInteger
import java.security.*


class RingSignatureCreator {

    companion object {

        private val random = SecureRandom()

        fun sign(message: String, signerKeyPair: KeyPair, publicKeys: List<PublicKey>): RingSignature {
            val ringSize = publicKeys.size + 1
            val hashedMessage = cryptoHash(message)
            val ringTag = createRingTag(signerKeyPair)
            val participantsRingValues = List(ringSize - 1) { BigInteger(1024, random) }
            val startGlueValue = BigInteger(1024, random)
            val initialGlueValues = listOf(keyedHash(hashedMessage, startGlueValue.toString()))
            val glueValues = participantsRingValues.zip(publicKeys).fold(initialGlueValues) { glueValues, (userValue, key) ->
                val encryptedValue = rsaEncrypt(userValue, key)
                val withGlueValue = encryptedValue xor glueValues.last()
                val withRingTag = withGlueValue xor ringTag
                glueValues + keyedHash(hashedMessage, withRingTag.toString())
            }

            val expectedValue = glueValues.last() xor startGlueValue xor ringTag
            val signerRingValue = rsaDecrypt(expectedValue, signerKeyPair.private)
            val allRingValues = participantsRingValues + signerRingValue
            val allPublicKeys = publicKeys + signerKeyPair.public

            val rotation = random.nextInt(ringSize)
            return RingSignature(
                keys = rotateList(allPublicKeys, rotation),
                startValue = rotateList(glueValues, rotation).first(),
                ringValues = rotateList(allRingValues, rotation),
                tag = ringTag
            )
        }

        private fun <T> rotateList(list: List<T>, rotation: Int): List<T> {
            return list.takeLast(rotation) + list.dropLast(rotation)
        }

        private fun createRingTag(keyPair: KeyPair): BigInteger {
            val hashedPublicKey = cryptoHash(keyPair.public.encoded.copyOfRange(0, 100))
            val decrypted = rsaDecrypt(hashedPublicKey, keyPair.private)
            return cryptoHash(decrypted.toByteArray())
        }

    }

}

