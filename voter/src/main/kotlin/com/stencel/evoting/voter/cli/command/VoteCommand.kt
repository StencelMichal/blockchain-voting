package com.stencel.evoting.voter.cli.command

import com.n1analytics.paillier.PaillierPublicKey
import com.stencel.evoting.smartcontracts.VotingState
import com.stencel.evoting.smartcontracts.ringSignature.RingSignature
import com.stencel.evoting.smartcontracts.util.CryptographyUtils
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
import java.security.spec.RSAPublicKeySpec
import java.util.*

@Component
@CommandLine.Command(
    name = "vote",
    description = ["Vote for a candidate"]
)
class VoteCommand(
    private val votingService: VotingService,
    private val contractAddressResolverService: ContractAddressResolverService,
    private val web3j: Web3j,
    private val gasProvider: ContractGasProvider
) : Command() {

    @CommandLine.Option(names = ["--id"], required = false, description = ["Identifier of voting"])
    private var votingIdentifier: String = "test"

    @CommandLine.Option(names = ["--pk"], required = false, description = ["Voter private key"])
    private var privateKey: String = "0xda8d4ec5adbbf950d78b93eb73469361b73683b073eae8ac14c0cc025dbc8deb"

    @CommandLine.Option(names = ["--signatureSize"], required = false, description = ["Size of ring signature"])
    private var signatureSize: Int = 3

    override fun execute() {
        resolveContractAndPerform(votingIdentifier, privateKey) { votingStateContract ->
            Credentials.create(privateKey)
            val encryptionKey = getEncryptionKey(votingStateContract)
            val voterQuestions = votingStateContract.voterQuestions.send() as List<String>
            val candidates = votingStateContract.candidates.send() as List<String>
            val candidateIndex = selectCandidate(candidates)
            val voterAnswers = collectAnswers(voterQuestions)
            val vote = createVote(votingStateContract, candidateIndex, candidates.size, encryptionKey, voterAnswers)
            votingService.vote(votingStateContract, vote)
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
        commonEncryptionKey: PaillierPublicKey,
        voterAnswers: List<Int>,
    ): VotingState.Vote {
        val (encodedCiphertexts, encodedExponents) = encodeCandidate(
            candidateIndex,
            candidatesAmount,
            commonEncryptionKey
        )
        val encodedAnswers = encodeVoterAnswers(voterAnswers, commonEncryptionKey)
        val decoder = Base64.getDecoder()
        val keyPair = CryptographyUtils.generateRsaKeyPair()
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
        val ringSignature = createVoteSignature(keyPair, publicKeys)
        return VotingState.Vote(
            encodedCiphertexts,
            encodedExponents,
            encodedAnswers,
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

    private fun encodeVoterAnswers(answers: List<Int>, commonEncryptionKey: PaillierPublicKey): List<String> {
        val paillierSignedContext = commonEncryptionKey.createSignedContext()
        return answers.map { paillierSignedContext.encrypt(it.toLong()).calculateCiphertext().toString() }
    }

    private fun createVoteSignature(keyPair: KeyPair, publicKeys: List<PublicKey>): RingSignature {
        //TODO change message to vote content
        return RingSignature.create(
            "Message",
            keyPair,
            publicKeys
        )
    }

}