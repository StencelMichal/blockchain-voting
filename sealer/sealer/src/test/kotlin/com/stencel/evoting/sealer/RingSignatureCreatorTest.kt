package com.stencel.evoting.sealer

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.security.Security


class RingSignatureCreatorTest {

    @Test
    fun `it should pass multiple attempts of verifying signature`() {
        Security.addProvider(BouncyCastleProvider())
        val attemptsAmount = 20
        val ringSize = 10
        List(attemptsAmount) { _ ->
            val message = "hello world!"
            val signerKeyPair = CryptographyUtils.generateRsaKeyPair()
            val publicKeys = List(ringSize - 1) { CryptographyUtils.generateRsaKeyPair().public }
            val signature = RingSignature.create(message, signerKeyPair, publicKeys)
            val isVerified = signature.verify(message)
            assertEquals(true, isVerified)
        }
    }

    @Test
    fun `it should fail verification for different message`() {
        Security.addProvider(BouncyCastleProvider())
        val signerKeyPair = CryptographyUtils.generateRsaKeyPair()
        val publicKeys = List(10) { CryptographyUtils.generateRsaKeyPair().public }
        val creationMessage = "creation_message"
        val verificationMessage = "verification_message"
        val signature = RingSignature.create(creationMessage, signerKeyPair, publicKeys)
        val isVerified = signature.verify(verificationMessage)
        assertEquals(false, isVerified)
    }

}