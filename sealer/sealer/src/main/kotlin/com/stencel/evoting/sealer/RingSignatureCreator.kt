package com.stencel.evoting.sealer

import java.math.BigInteger
import java.security.*
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec


class RingSignatureCreator {


//    fun genKeyPair(): Map<String, BigInteger> {
//        val r = java.security.KeyPairGenerator.getInstance("RSA").apply {
//            initialize(2048, SecureRandom())
//        }.generateKeyPair()
//        return mapOf(
//            "e" to (r.public as java.security.interfaces.RSAPublicKey).publicExponent,
//            "d" to (r.private as java.security.interfaces.RSAPrivateKey).privateExponent,
//            "n" to (r.public as java.security.interfaces.RSAPublicKey).modulus
//        )
//    }
//
//    fun genPublicKey(): Map<String, BigInteger> {
//        val r = java.security.KeyPairGenerator.getInstance("RSA").apply {
//            initialize(2048, SecureRandom())
//        }.generateKeyPair()
//        return mapOf(
//            "e" to (r.public as java.security.interfaces.RSAPublicKey).publicExponent,
//            "n" to (r.public as java.security.interfaces.RSAPublicKey).modulus
//        )
//    }

    fun sign(message: String, signerKeyPair: KeyPair, publicKeys: List<PublicKey>): Map<String, Any> {
        val ringSize = publicKeys.size + 1
        val hashedMessage = cryptoHash(message)
        val startGlueValue = BigInteger(1024, SecureRandom())
        val v = MutableList<BigInteger>(ringSize) { BigInteger.ZERO }
        v[0] = keyedHash(hashedMessage, startGlueValue.toString())

        val userValues = mutableListOf<BigInteger>().apply {
            add(BigInteger.ZERO)
            addAll((1 until ringSize).map { BigInteger(1024, SecureRandom()) })
        }

        for (i in 1 until ringSize) {
            v[i] = keyedHash(
                hashedMessage,
                xor(
                    v[i - 1],
                    rsaEncrypt(userValues[i], publicKeys[i - 1])
                ).toString()
            )
        }
        userValues[0] = rsaDecrypt(xor(v[ringSize - 1], startGlueValue), signerKeyPair.private)

        val signatureRows = mutableListOf<Map<String, Any>>().apply {
            add(
                mapOf(
                    "e" to (signerKeyPair.public as java.security.interfaces.RSAPublicKey).publicExponent,
                    "n" to (signerKeyPair.public as java.security.interfaces.RSAPublicKey).modulus,
                    "userValues" to userValues[0]
                )
            )
            addAll((1 until ringSize).map { i ->
                mapOf(
                    "e" to (publicKeys[i - 1] as java.security.interfaces.RSAPublicKey).publicExponent,
                    "n" to (publicKeys[i - 1] as java.security.interfaces.RSAPublicKey).modulus,
                    "userValues" to userValues[i]
                )
            })
        }

        val rotation = 0
        val rotatedV = v.takeLast(rotation) + v.dropLast(rotation)
        val rotatedRows = signatureRows.takeLast(rotation) + signatureRows.dropLast(rotation)

        return mapOf(
            "msg" to message,
            "rows" to rotatedRows,
            "v" to rotatedV.last()
        )
    }


    fun cryptoHash(msg: String): BigInteger {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(msg.toByteArray())
        return BigInteger(1, hashBytes)
    }

    fun keyedHash(key: BigInteger, msg: String): BigInteger {
        val hmac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(), "HmacSHA256")
        hmac.init(secretKey)
        val hashBytes = hmac.doFinal(msg.toByteArray())
        return BigInteger(1, hashBytes)
    }

    fun rsaEncryptOrDecrypt(msg: BigInteger, eOrD: BigInteger, n: BigInteger): BigInteger {
        return msg.modPow(eOrD, n)
    }

    fun rsaDecrypt(msg: BigInteger, privateKey: PrivateKey): BigInteger {
        val dec = Cipher.getInstance("RSA/ECB/NoPadding")
        dec.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedContentKey = dec.doFinal(msg.toByteArray())
        return BigInteger(1, decryptedContentKey)
    }

    fun rsaEncrypt(msg: BigInteger, publicKey: PublicKey): BigInteger {
//        val spec = RSAPublicKeySpec(n, e)
//        val factory = KeyFactory.getInstance("RSA");
//        val pub = factory.generatePublic(spec)
        val enc = Cipher.getInstance("RSA/ECB/NoPadding")
        enc.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedContentKey = enc.doFinal(msg.toByteArray())
        return BigInteger(1, encryptedContentKey)
    }

    fun xor(a: BigInteger, b: BigInteger): BigInteger {
        return a.xor(b)
    }


    fun verify(signature: Map<String, Any>): Boolean {
        val ringSize = (signature["rows"] as List<*>).size
        val key = cryptoHash(signature["msg"] as String)
        var v = signature["v"] as BigInteger

        (signature["rows"] as List<*>).forEach { row ->
            val e = (row as Map<String, BigInteger>)["e"]!!
            val n = row["n"]!!
            val s = row["userValues"] as BigInteger
            v = keyedHash(key, xor(v, rsaEncryptOrDecrypt(s, e, n)).toString())
        }
        return v == signature["v"]
    }


}

