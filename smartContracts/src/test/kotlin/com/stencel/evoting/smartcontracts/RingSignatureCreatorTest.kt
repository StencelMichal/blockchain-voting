package com.stencel.evoting.smartcontracts

import com.stencel.evoting.smartcontracts.ringSignature.RingSignature
import com.stencel.evoting.smartcontracts.util.CryptographyUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.security.Security


class RingSignatureCreatorTest {

    @Test
    fun `it should pass multiple attempts of verifying signature`() {
        Security.addProvider(BouncyCastleProvider())
        val attemptsAmount = 20
        val ringSize = 10
        List(attemptsAmount) { attempt ->
            println("Attempt: $attempt")
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

    @Test
    fun `two signatures created with the same private key should have same tag`() {
        Security.addProvider(BouncyCastleProvider())
        val signerKeyPair = CryptographyUtils.generateRsaKeyPair()
        val publicKeys1 = List(10) { CryptographyUtils.generateRsaKeyPair().public }
        val publicKeys2 = List(10) { CryptographyUtils.generateRsaKeyPair().public }
        val signature1 = RingSignature.create("message1", signerKeyPair, publicKeys1)
        val signature2 = RingSignature.create("message2", signerKeyPair, publicKeys2)
        assertEquals(signature1.tag, signature2.tag)
    }

    @Test
    fun `two signatures create with different key should have different tag`() {
        Security.addProvider(BouncyCastleProvider())
        val signerKeyPair1 = CryptographyUtils.generateRsaKeyPair()
        val signerKeyPair2 = CryptographyUtils.generateRsaKeyPair()
        val publicKeys = List(10) { CryptographyUtils.generateRsaKeyPair().public }
        val message = "test message"
        val signature1 = RingSignature.create(message, signerKeyPair1, publicKeys)
        val signature2 = RingSignature.create(message, signerKeyPair2, publicKeys)
        assertNotEquals(signature1.tag, signature2.tag)
    }

}