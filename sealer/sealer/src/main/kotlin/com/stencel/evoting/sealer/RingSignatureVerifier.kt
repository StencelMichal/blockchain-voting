package com.stencel.evoting.sealer

import com.stencel.evoting.sealer.CryptographyUtils.Companion.calculateHMac
import com.stencel.evoting.sealer.CryptographyUtils.Companion.encrypt
import com.stencel.evoting.sealer.CryptographyUtils.Companion.xorByteArrays
import java.util.*

class RingSignatureVerifier {
    companion object {
        fun verifyRingSignature(message: String, ringSignature: RingSignature): Boolean {
            val hashedMessage = CryptographyUtils.hash(message)
            val startGlueValue = Base64.getDecoder().decode(ringSignature.startValue)
            var glueValue = startGlueValue
            ringSignature.keys.zip(ringSignature.ringValues).forEach { x ->
                println("RING VALUE: ${Base64.getEncoder().encodeToString(glueValue)}")
                val key = x.first
                val userValue = x.second
                val userValueBytes = Base64.getDecoder().decode(userValue)
                val encrypted = encrypt(userValueBytes, key)
                val xored = xorByteArrays(glueValue, encrypted)
                glueValue = calculateHMac(truncateLeadingZeros(xored), hashedMessage)
            }
            println("LAST RING VALUE: ${Base64.getEncoder().encodeToString(glueValue)}")
            return glueValue.contentEquals(startGlueValue)
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
    }
}