package com.stencel.evoting.sealer

import com.stencel.evoting.sealer.CryptographyUtils.Companion.cryptoHash
import com.stencel.evoting.sealer.CryptographyUtils.Companion.rsaEncrypt
import com.stencel.evoting.sealer.CryptographyUtils.Companion.keyedHash
import com.stencel.evoting.sealer.CryptographyUtils.Companion.rsaDecrypt
import java.math.BigInteger
import java.security.*


class RingSignatureCreator {

    companion object {

        private val random = SecureRandom()

        fun sign(message: String, signerKeyPair: KeyPair, publicKeys: List<PublicKey>): RingSignature {
            val ringSize = publicKeys.size + 1
            val hashedMessage = cryptoHash(message)
            val participantsRingValues = List(ringSize - 1) { BigInteger(1024, random) }
            val startGlueValue = BigInteger(1024, random)
            val initialGlueValues = listOf(keyedHash(hashedMessage, startGlueValue.toString()))
            val glueValues = participantsRingValues.zip(publicKeys).fold(initialGlueValues) { glueValues, (userValue, key) ->
                val encryptedValue = rsaEncrypt(userValue, key)
                val xored = glueValues.last() xor encryptedValue
                glueValues + keyedHash(hashedMessage, xored.toString())
            }

            val signerRingValue = rsaDecrypt(glueValues.last() xor startGlueValue, signerKeyPair.private)
            val allRingValues = participantsRingValues + signerRingValue
            val allPublicKeys = publicKeys + signerKeyPair.public

            val rotation = random.nextInt(ringSize)
            return RingSignature(
                keys = rotateList(allPublicKeys, rotation),
                startValue = rotateList(glueValues, rotation).first(),
                ringValues = rotateList(allRingValues, rotation)
            )
        }

        private fun <T> rotateList(list: List<T>, rotation: Int): List<T> {
            return list.takeLast(rotation) + list.dropLast(rotation)
        }

    }

}

