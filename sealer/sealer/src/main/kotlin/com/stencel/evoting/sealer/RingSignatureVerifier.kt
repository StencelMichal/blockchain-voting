package com.stencel.evoting.sealer

import com.stencel.evoting.sealer.CryptographyUtils.Companion.cryptoHash
import com.stencel.evoting.sealer.CryptographyUtils.Companion.rsaEncrypt
import com.stencel.evoting.sealer.CryptographyUtils.Companion.keyedHash

class RingSignatureVerifier {
    companion object {

        fun verifyRingSignature(message: String, signature: RingSignature): Boolean {
            val hashedMessage = cryptoHash(message)
            val lastGlueValue = signature.ringValues.zip(signature.keys).fold(signature.startValue) { glueValue, (ringValue, key) ->
                val encryptedValue = rsaEncrypt(ringValue, key)
                val xored = glueValue xor encryptedValue
                keyedHash(hashedMessage, xored.toString())
            }
            return lastGlueValue == signature.startValue
        }

    }
}