package com.stencel.evoting.smartcontracts.ringSignature

import com.stencel.evoting.smartcontracts.VotingState
import java.math.BigInteger
import java.security.KeyPair
import java.security.PublicKey
import java.util.*

data class RingSignature(
    val keys: List<PublicKey>,
    val startValue: BigInteger,
    val ringValues: List<BigInteger>,
    val tag: BigInteger,
) {
    val size = keys.size

    private val encoder = Base64.getEncoder()

    fun verify(message: String): Boolean {
        return RingSignatureVerifier.verifyRingSignature(message, this)
    }

    fun toDto(): VotingState.RingSignature {
        return VotingState.RingSignature(
            encoder.encodeToString(startValue.toByteArray()),
            keys.map { it as java.security.interfaces.RSAPublicKey }.map {
                encoder.encodeToString(it.modulus.toByteArray())
            },
            keys.map { it as java.security.interfaces.RSAPublicKey }.map {
                encoder.encodeToString(it.publicExponent.toByteArray())
            },
            ringValues.map { encoder.encodeToString(it.toByteArray()) },
            encoder.encodeToString(tag.toByteArray())
        )
    }

    companion object {

        fun create(message: String, signerKeyPair: KeyPair, publicKeys: List<PublicKey>): RingSignature {
            return RingSignatureCreator.sign(message, signerKeyPair, publicKeys)
        }
    }
}
