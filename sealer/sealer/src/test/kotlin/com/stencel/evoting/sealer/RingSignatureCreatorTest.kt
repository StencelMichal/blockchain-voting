package com.stencel.evoting.sealer

import com.stencel.evoting.sealer.CryptographyUtils.Companion.encrypt
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.Security
import javax.crypto.Cipher


class RingSignatureCreatorTest {

    private val random = SecureRandom()

//    @Test
//    fun `createRingSignature should return RingSignature`() {
//        Security.addProvider(BouncyCastleProvider())
//        val message = "test_message"
//        val signerKeyPair = CryptographyUtils.generateRsaKeyPair()
//        val publicKeys =
//            listOf(CryptographyUtils.generateRsaKeyPair().public, CryptographyUtils.generateRsaKeyPair().public)
//        val ringSignature = RingSignatureCreatorOld.createRingSignature(message, signerKeyPair, publicKeys)
//
//        println("---- VERIFICATION -----")
//        val verificationResult = RingSignatureVerifier.verifyRingSignature(message, ringSignature)
//        println(verificationResult)
//
//    }

    @Test
    fun `asd`() {
        Security.addProvider(BouncyCastleProvider())
        val message = "test_message".toByteArray()
        val generateRsaKeyPair = CryptographyUtils.generateRsaKeyPair()
        val encrypted = encrypt(message, generateRsaKeyPair.public)
        val decrypted = decrypt(encrypted, generateRsaKeyPair.private)
        val message1 = "RESULT: " + String(message) + "  " + String(decrypted)
        println(message1)

    }

    @Test
    fun `it should pass multiple attempts of verifying signature`() {
        Security.addProvider(BouncyCastleProvider())
        val attemptsAmount = 20
        val ringSize = 10
        val verifiedSignaturesAmount = List(attemptsAmount) { _ ->
            val message = "hello world!"
            val signerKeyPair = CryptographyUtils.generateRsaKeyPair()
            val publicKeys = List(ringSize - 1) { CryptographyUtils.generateRsaKeyPair().public }
            val signature = RingSignatureCreator().sign(message, signerKeyPair, publicKeys)
            val isVerified = RingSignatureCreator().verify(signature)
            if (isVerified) 1 else 0
        }.sum()
        assertEquals(attemptsAmount, verifiedSignaturesAmount)
    }

//    @Test
//    fun `it should detect invalid signature`() {
//        Security.addProvider(BouncyCastleProvider())
//        val signerKeyPair = genKeyPair()
//        val publicKeys = List(9) { genPublicKey() }
//        val signature = RingSignatureCreator().sign("hello world!", signerKeyPair, publicKeys)
//        val isVerified = RingSignatureCreator().verify(signature)
//        assertEquals(false, isVerified)
//    }

    fun genKeyPair(): Map<String, BigInteger> {
        val r = java.security.KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048, SecureRandom())
        }.generateKeyPair()
        return mapOf(
            "e" to (r.public as java.security.interfaces.RSAPublicKey).publicExponent,
            "d" to (r.private as java.security.interfaces.RSAPrivateKey).privateExponent,
            "n" to (r.public as java.security.interfaces.RSAPublicKey).modulus
        )
    }

    fun genPublicKey(): Map<String, BigInteger> {
        val r = java.security.KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048, SecureRandom())
        }.generateKeyPair()
        return mapOf(
            "e" to (r.public as java.security.interfaces.RSAPublicKey).publicExponent,
            "n" to (r.public as java.security.interfaces.RSAPublicKey).modulus
        )
    }

    private fun randomByteArray(size: Int): ByteArray {
        val byteArray = ByteArray(size)
        random.nextBytes(byteArray)
        return byteArray
    }

    private fun decrypt(cipherText: ByteArray, privateKey: PrivateKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/None/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(cipherText)
    }


}