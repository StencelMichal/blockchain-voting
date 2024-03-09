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
                val withGlueValue = encryptedValue xor glueValue
                val withRingTag = withGlueValue xor signature.tag
                keyedHash(hashedMessage, withRingTag.toString())
            }
            return lastGlueValue == signature.startValue
        }

    }
}