package com.stencel.evoting.sealer

import com.stencel.evoting.smartcontracts.CryptographyUtils.Companion.cryptoHash
import com.stencel.evoting.smartcontracts.CryptographyUtils.Companion.rsaEncrypt
import com.stencel.evoting.smartcontracts.CryptographyUtils.Companion.keyedHash
import java.util.*

class RingSignatureVerifier {
    companion object {

        @OptIn(ExperimentalStdlibApi::class)
        fun verifyRingSignature(message: String, signature: RingSignature): Boolean {
            val hashedMessage = cryptoHash(message)
            val encoder = Base64.getEncoder()
            println("HASH VALUE: ")
            println(encoder.encodeToString(message.toByteArray()))
            println(hashedMessage.toByteArray().toHexString())
            println(encoder.encodeToString(hashedMessage.toByteArray()))
            println("RING VERIFICATION LOCAL")
            val lastGlueValue = signature.ringValues.zip(signature.keys).fold(signature.startValue) { glueValue, (ringValue, key) ->
                println(ringValue.toByteArray().toHexString())
                val encryptedValue = rsaEncrypt(ringValue, key)
                val withGlueValue = encryptedValue xor glueValue
                val withRingTag = withGlueValue xor signature.tag
                keyedHash(hashedMessage, withRingTag.toString())
            }
            return lastGlueValue == signature.startValue
        }

    }
}