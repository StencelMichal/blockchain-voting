package com.stencel.evoting.sealer

import java.math.BigInteger
import java.security.KeyPair
import java.security.PublicKey

data class RingSignature(
    val keys: List<PublicKey>,
    val startValue: BigInteger,
    val ringValues: List<BigInteger>,
) {
    val size = keys.size

    fun verify(message: String): Boolean {
        return RingSignatureVerifier.verifyRingSignature(message, this)
    }

    companion object {

        fun create(message: String, signerKeyPair: KeyPair, publicKeys: List<PublicKey>): RingSignature {
            return RingSignatureCreator.sign(message, signerKeyPair, publicKeys)
        }
    }
}
