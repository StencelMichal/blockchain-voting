package com.stencel.evoting.voter

import com.n1analytics.paillier.PaillierPublicKey
import com.stencel.evoting.smartcontracts.util.CryptographyUtils.Companion.generateRsaKeyPair
import com.stencel.evoting.smartcontracts.ringSignature.RingSignature
import com.stencel.evoting.smartcontracts.VotingState
import com.stencel.evoting.smartcontracts.configuration.Web3jConfiguration
import com.stencel.evoting.smartcontracts.service.VotingStateManagerService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.web3j.crypto.Credentials
import org.web3j.tuples.generated.Tuple2
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*

@SpringBootApplication(scanBasePackages = ["com.stencel.evoting.voter", "com.stencel.evoting.smartcontracts"])
@EntityScan(basePackages = ["com.stencel.evoting.smartcontracts.database"])
@EnableJpaRepositories("com.stencel.evoting.smartcontracts.database")
class VoterApplication

fun main(args: Array<String>) {
    val context = runApplication<VoterApplication>(*args)
    val web3jConf = context.getBean(Web3jConfiguration::class.java)
    val votingStateManagerService = context.getBean(VotingStateManagerService::class.java)
    val votingStateContract = votingStateManagerService.contract
    val commonEncryptionKey = votingStateContract.commonEncryptionKey().send()
    val voterQuestions = votingStateContract.voterQuestions.send() as List<String>
    val candidates = votingStateContract.candidates.send() as List<String>
    val credentials = getVoterCredentials()
    val candidateIndex = selectCandidate(candidates)
    val voterAnswers = collectAnswers(voterQuestions)
    val vote = createVote(votingStateContract, candidateIndex, candidates.size, commonEncryptionKey, voterAnswers)
    votingStateContract.vote(vote).send()
}

private fun getVoterCredentials(): Credentials {
    println("Enter private key:")
    val privateKey = readln()
    return Credentials.create(privateKey)
}

private fun selectCandidate(candidates: List<String>): Int {
    println("Select candidate:")
    candidates.forEachIndexed { index, candidate -> println("$index: $candidate") }
    return readln().toInt()
}

private fun collectAnswers(questions: List<String>): List<Int> {
    return questions.fold(listOf()) { answers, question ->
        println(question)
        var binaryAnswer = readln().toInt()
        while (binaryAnswer != 1 && binaryAnswer != 0) {
            println("Invalid answer. Please enter 0 or 1.")
            binaryAnswer = readln().toInt()
        }
        answers + binaryAnswer
    }
}

private fun createVote(
    votingStateContract: VotingState,
    candidateIndex: Int,
    candidatesAmount: Int,
    commonEncryptionKey: Tuple2<String, String>,
    voterAnswers: List<Int>
): VotingState.Vote {
    val encodedVoteContent = encodeCandidate(candidateIndex, candidatesAmount, commonEncryptionKey)
    val encodedAnswers = encodeVoterAnswers(voterAnswers, commonEncryptionKey)
    val decoder = Base64.getDecoder()
    val keyPair = generateRsaKeyPair()
    val publicKeys =
        List(3) { id ->
            votingStateContract.votersPublicKeys(BigInteger.valueOf(id.toLong())).send() }.map { rsaPublicKey ->
            val keySpec = RSAPublicKeySpec(
                BigInteger(decoder.decode(rsaPublicKey.component1())),
                BigInteger(decoder.decode(rsaPublicKey.component2()))
            )
            val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(keySpec)
        }
    val ringSignature = createVoteSignature(keyPair, publicKeys)
    return VotingState.Vote(
        encodedVoteContent,
        encodedAnswers,
        ringSignature.toDto()
    )
}

private fun encodeCandidate(
    candidateIndex: Int,
    candidatesAmount: Int,
    commonEncryptionKey: Tuple2<String, String>
): List<String> {
    val encodedVote = Array(candidatesAmount) { 0 }
    encodedVote[candidateIndex] = 1
    val paillierPublicKey = PaillierPublicKey(BigInteger(Base64.getDecoder().decode(commonEncryptionKey.component1())))
    val paillierSignedContext = paillierPublicKey.createSignedContext()
    return encodedVote.map { paillierSignedContext.encrypt(it.toLong()).calculateCiphertext().toString() }
}

private fun encodeVoterAnswers(answers: List<Int>, commonEncryptionKey: Tuple2<String, String>): List<String> {
    val paillierPublicKey = PaillierPublicKey(BigInteger(Base64.getDecoder().decode(commonEncryptionKey.component1())))
    val paillierSignedContext = paillierPublicKey.createSignedContext()
    return answers.map { paillierSignedContext.encrypt(it.toLong()).calculateCiphertext().toString() }
}

private fun createVoteSignature(keyPair: KeyPair, publicKeys: List<PublicKey>): RingSignature {
    return RingSignature.create(
        "DUPA",
        keyPair,
        publicKeys
    )
}




