package com.stencel.evoting.voter.cli.command

import com.n1analytics.paillier.PaillierPublicKey
import com.stencel.evoting.smartcontracts.VotingState
import com.stencel.evoting.smartcontracts.ringSignature.RingSignature
import com.stencel.evoting.smartcontracts.util.CryptographyUtils
import com.stencel.evoting.smartcontracts.util.VoteContent
import com.stencel.evoting.voter.service.ContractAddressResolverService
import com.stencel.evoting.voter.service.VotingService
import org.springframework.stereotype.Component
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tuples.generated.Tuple2
import org.web3j.tx.gas.ContractGasProvider
import picocli.CommandLine
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PublicKey
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import java.util.*

@Component
@CommandLine.Command(
    name = "vote",
    description = ["Vote for a candidate"],
    mixinStandardHelpOptions = true
)
class VoteCommand(
    private val votingService: VotingService,
    private val contractAddressResolverService: ContractAddressResolverService,
    private val web3j: Web3j,
    private val gasProvider: ContractGasProvider,
) : Command() {

    @CommandLine.Option(names = ["--id"], required = false, description = ["Identifier of voting"])
    private var votingIdentifier: String = "test"

    @CommandLine.Option(names = ["--pk"], required = false, description = ["Voter private key"])
    private var privateKey: String = "0xda8d4ec5adbbf950d78b93eb73469361b73683b073eae8ac14c0cc025dbc8deb"

    @CommandLine.Option(names = ["--signatureSize"], required = false, description = ["Size of ring signature"])
    private var signatureSize: Int = 3

    @CommandLine.Option(names = ["--modulus"], required = false, description = ["Modulus of voters key"])
    private var modulus =
        "AMuxTWVy2eqWLzVJMl5QPBNIURiaG/7nT9HLt3RnTbMmk5WwwCtnHwtfNTTNv4l28ZNTUsE/YMXNA49gymRQyGahDNPW2o8qYBROmOFGqNgOsFeDcFDqWqI1n+UZM4vlimA+dDh5Co/2dKZQ3+4cc96ca2VvcSPwG1i8LxzFX5UwyFI1i9Ai6m6G55ikOepUIpPcLuJ15FYrzt52JaNNuqDCoxgJ9jnsNqsrpA9rhVoeDwGjcnt9+UYmOOx0N+jykkh1kvjEd/Z+wLf5y3SMb/f9Gw4fbbN1Sg6243QuHqYV2xgarmphoDgGkMOmr49whBs+RgofVXAAilV8lDcUXe8="

    @CommandLine.Option(
        names = ["--publicExponent"],
        required = false,
        description = ["Public exponent of voter's key"]
    )
    private var publicExponent = "AQAB"

    @CommandLine.Option(
        names = ["--privateExponent"],
        required = false,
        description = ["Private exponent of voter's key"]
    )
    private var privateExponent =
        "CqDELIxrEWHnsayRi7k9ATaPQKzd1BWGpSgveMvhEn1rSu6vgDQ/uuyrToeDvGzv2uOImFuxtXBmhKckuEo8wpoZnL4Dpl+sJrMZJ/vzWF6f1dkeVaJ8uyT4JFCFz4FZEH+Bueaa5fsSiBEFNhvW8eEQe3jumtTu4FjlTmd//r02dWpAhqCgZpUgTJxAOh5jvukwgiI55sHuGhIO3czWbXyCyzYZ+hd1YwSW7jNtCBVlFRvhCjm1AIBeLjHDFGG/I7L22O+BZLpVFyDA4zXMItBgJZzmnqxVadyT4rzV7PcwGsO4FvEgof0fIm2g4fp1xJfoi1+UTpSJPWSQwb+lqQ=="

    private val decoder = Base64.getDecoder()

    override fun execute() {
        resolveContractAndPerform(votingIdentifier, privateKey) { votingStateContract ->
            Credentials.create(privateKey)
            val encryptionKey = getEncryptionKey(votingStateContract)
            val voterQuestions = votingStateContract.voterQuestions.send() as List<String>
            val candidates = votingStateContract.candidates.send() as List<String>
            val candidateIndex = selectCandidate(candidates)
            val voterAnswers = collectAnswers(voterQuestions)
            val voterKeyPair = reconstructKeyPair(
                BigInteger(decoder.decode(modulus)),
                BigInteger(decoder.decode(privateExponent)),
                BigInteger(decoder.decode(publicExponent))
            )
            val vote = createVote(
                voterKeyPair,
                votingStateContract,
                candidateIndex,
                candidates.size,
                encryptionKey,
                voterAnswers
            )
            votingService.vote(votingStateContract, vote)
            println("Vote has been cast and awaits validation")
        }
    }

    private fun <T> resolveContractAndPerform(
        votingIdentifier: String,
        blockchainPrivateKey: String,
        action: (VotingState) -> T,
    ): T? {
        val credentials = Credentials.create(blockchainPrivateKey)
        return contractAddressResolverService.resolveContractAddress(votingIdentifier)?.let { address ->
            val contract = VotingState.load(address, web3j, credentials, gasProvider)
            action(contract)
        }
    }

    private fun reconstructKeyPair(
        modulus: BigInteger,
        privateExponent: BigInteger,
        publicExponent: BigInteger,
    ): KeyPair {
        val publicKeySpec = RSAPublicKeySpec(modulus, publicExponent)
        val privateKeySpec = RSAPrivateKeySpec(modulus, privateExponent)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(publicKeySpec)
        val privateKey = keyFactory.generatePrivate(privateKeySpec)
        return KeyPair(publicKey, privateKey)
    }


    private fun getEncryptionKey(contract: VotingState): PaillierPublicKey {
        val commonEncryptionKey = contract.commonEncryptionKey().send()
        val modulus = BigInteger(Base64.getDecoder().decode(commonEncryptionKey.component1()))
        return PaillierPublicKey(modulus)
    }


    private fun selectCandidate(candidates: List<String>): Int {
        println("Select candidate:")
        candidates.forEachIndexed { index, candidate -> println("$index: $candidate") }
        return readln().toInt()
    }

    private fun collectAnswers(questions: List<String>): List<Int> {
        return questions.foldIndexed(listOf()) { id, answers, question ->
            println("Question ${id + 1}: $question [YES/NO]")
            val answer = getAnswer()
            val binaryAnswer = if (answer) 1 else 0
            answers + binaryAnswer
        }
    }

    private fun getAnswer(): Boolean {
        while (true) {
            val answer = readln().uppercase()
            if (answer == "YES") return true
            if (answer == "NO") return false
            println("Invalid answer. Please enter YES or NO.")
        }

    }

    private fun createVote(
        keyPair: KeyPair,
        votingStateContract: VotingState,
        candidateIndex: Int,
        candidatesAmount: Int,
        commonEncryptionKey: PaillierPublicKey,
        voterAnswers: List<Int>,
    ): VotingState.Vote {
        val (encodedCiphertexts, encodedExponents) = encodeCandidate(
            candidateIndex,
            candidatesAmount,
            commonEncryptionKey
        )
        val (encodedAnswers, encodedAnswersExponents) = encodeVoterAnswers(voterAnswers, commonEncryptionKey)
        val decoder = Base64.getDecoder()
        val publicKeys =
            List(signatureSize) { id ->
                votingStateContract.votersPublicKeys(BigInteger.valueOf(id.toLong())).send()
            }.map { rsaPublicKey ->
                val keySpec = RSAPublicKeySpec(
                    BigInteger(decoder.decode(rsaPublicKey.component1())),
                    BigInteger(decoder.decode(rsaPublicKey.component2()))
                )
                val keyFactory: KeyFactory = KeyFactory.getInstance("RSA")
                keyFactory.generatePublic(keySpec)
            }
        val ringSignature = createVoteSignature(
            encodedCiphertexts,
            encodedExponents,
            encodedAnswers,
            encodedAnswersExponents,
            keyPair,
            publicKeys
        )
        return VotingState.Vote(
            encodedCiphertexts,
            encodedExponents,
            encodedAnswers,
            encodedAnswersExponents,
            ringSignature.toDto()
        )
    }

    private fun encodeCandidate(
        candidateIndex: Int,
        candidatesAmount: Int,
        commonEncryptionKey: PaillierPublicKey,
    ): Tuple2<List<String>, List<BigInteger>> {
        val encodedVote = Array(candidatesAmount) { 0 }
        encodedVote[candidateIndex] = 1
        val paillierSignedContext = commonEncryptionKey.createSignedContext()
        val encryptedValues = encodedVote.map { paillierSignedContext.encrypt(it.toLong()) }
        val ciphertexts = encryptedValues.map { it.calculateCiphertext().toString() }
        val exponents = encryptedValues.map { it.exponent.toBigInteger() }
        return Tuple2(ciphertexts, exponents)
    }

    private fun encodeVoterAnswers(
        answers: List<Int>,
        commonEncryptionKey: PaillierPublicKey
    ): Tuple2<List<String>, List<BigInteger>> {
        val paillierSignedContext = commonEncryptionKey.createSignedContext()
        val encryptedValues = answers.map { paillierSignedContext.encrypt(it.toLong()) }
        val encryptedAnswers = encryptedValues.map { it.calculateCiphertext().toString() }
        val exponents = encryptedValues.map { it.exponent.toBigInteger() }
        return Tuple2(encryptedAnswers, exponents)
    }

    private fun createVoteSignature(
        encryptedVotes: List<String>,
        encryptedExponents: List<BigInteger>,
        voterEncryptedAnswers: List<String>,
        encodedAnswersExponents: List<BigInteger>,
        keyPair: KeyPair,
        publicKeys: List<PublicKey>,
    ): RingSignature {
        val content =
            VoteContent(encryptedVotes, encryptedExponents, voterEncryptedAnswers, encodedAnswersExponents).toJson()
        return RingSignature.create(
            content,
            keyPair,
            publicKeys
        )
    }

}