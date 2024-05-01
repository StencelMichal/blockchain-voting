package com.stencel.evoting.sealer.service

import com.stencel.evoting.smartcontracts.VotingState
import com.stencel.evoting.smartcontracts.VotingState.RsaPublicKey
import com.stencel.evoting.smartcontracts.ringSignature.RingSignature
import com.stencel.evoting.smartcontracts.util.VoteContent
import org.springframework.stereotype.Service
import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*

@Service
class VoteValidatorService {

    private val base64Decoder = Base64.getDecoder()
    val rsaKeyFactory = KeyFactory.getInstance("RSA")

    fun validateVote(vote: VotingState.Vote): Boolean {
        try {
            val signature = toRingSignature(vote.ringSignature)
            val voteContent = VoteContent(
                vote.encryptedVotes,
                vote.encryptedExponents,
                vote.voterEncryptedAnswers,
                vote.encodedAnswersExponents
            ).toJson()
            return signature.verify(voteContent)
        } catch (e: Exception) {
            throw VoteValidationException("Vote validation failed")
        }
    }

    private fun toRingSignature(signatureDto: VotingState.RingSignature): RingSignature {
        return RingSignature(
            keys = toPublicKeys(signatureDto.moduluses_base64, signatureDto.exponents_base64),
            startValue = BigInteger(base64Decoder.decode(signatureDto.startValue)),
            ringValues = toRingValues(signatureDto.ringValues),
            tag = BigInteger(base64Decoder.decode(signatureDto.tag))
        )
    }

    private fun toPublicKeys(moduluses: List<String>, exponents: List<String>): List<PublicKey> {
        if (moduluses.size != exponents.size) {
            throw VoteValidationException("Moduluses and exponents lists must have the same size")
        }
        return moduluses.zip(exponents).map { (modulus_base64, exponent_base64) ->
            val modulus = BigInteger(base64Decoder.decode(modulus_base64))
            val exponent = BigInteger(base64Decoder.decode(exponent_base64))
            val spec = RSAPublicKeySpec(modulus, exponent)
            rsaKeyFactory.generatePublic(spec)
        }
    }

    private fun toRingValues(ringValues: List<String>): List<BigInteger> {
        return ringValues.map { BigInteger(base64Decoder.decode(it)) }
    }
}

class VoteValidationException(message: String) : Exception(message)
