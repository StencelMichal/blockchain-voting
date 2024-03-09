package com.stencel.evoting.sealer

import com.stencel.evoting.sealer.CryptographyUtils.Companion.cryptoHash
import com.stencel.evoting.sealer.CryptographyUtils.Companion.encrypt
import com.stencel.evoting.sealer.CryptographyUtils.Companion.keyedHash

class RingSignatureVerifier {
    companion object {

        fun verifyRingSignature(message: String, signature: RingSignature): Boolean {
            val hashedMessage = cryptoHash(message)
            var glueValue = signature.startValue
            signature.ringValues.zip(signature.keys).forEach { (ringValue, key) ->
                println("glueValue: $glueValue")
                val encryptedValue = encrypt(ringValue, key)
                val xored = glueValue xor encryptedValue
                glueValue = keyedHash(hashedMessage, xored.toString())
            }
            println("glueValue: $glueValue")
            return glueValue == signature.startValue
        }

    }
}