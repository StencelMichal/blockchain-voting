package com.stencel.evoting.sealer

import com.n1analytics.paillier.PaillierPrivateKey
import com.stencel.evoting.smartcontracts.util.CryptographyUtils.Companion.generateRsaKeyPair
import com.stencel.evoting.smartcontracts.VotingState
import com.stencel.evoting.smartcontracts.service.VotingStateManagerService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.StaticGasProvider
import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import javax.crypto.Cipher


@SpringBootApplication
class SealerApplication

fun main(args: Array<String>) {
    val context = runApplication<SealerApplication>(*args)
    val keypair = PaillierPrivateKey.create(1024)
    val signedContext = keypair.publicKey.createSignedContext()
    keypair.publicKey.serialize {  }
    val encryptedNumber = signedContext.encrypt(1L)
    val encryptedNumber2 = signedContext.encrypt(1L)
    println("Encrypted numbers:")
    println(encryptedNumber.calculateCiphertext())
    println(encryptedNumber2.calculateCiphertext())
    val decryptedNumber = keypair.decrypt(encryptedNumber).decodeLong()
    val decryptedNumber2 = keypair.decrypt(encryptedNumber2).decodeLong()
    println("Decrypted number: $decryptedNumber")
    println("Decrypted number: $decryptedNumber2")
//    val rawNumbers = listOf(0.0, 0.8, 1.0, 3.2, -5.0, 50.0)
//
//    val keypair1 = PaillierPrivateKey.create(1024)
//    val keypair2 = PaillierPrivateKey.create(1024)
//
//    val paillierContext1 = keypair1.publicKey.createSignedContext()
//    val paillierContext2 = keypair2.publicKey.createSignedContext()
//
//    println("Encrypting doubles with public key (e.g., on multiple devices)")
//    val encryptedNumbers1 = rawNumbers.map{n -> paillierContext1.encrypt(n).calculateCiphertext()}
//    val encryptedNumbers2 = encryptedNumbers1.map{n -> paillierContext2.encrypt(n)}
//
//    println("Adding encrypted doubles")
//    val encryptedSum = encryptedNumbers2.reduce{n1, n2 -> n1.add(n2)}
//
//    println("Decrypting result:")
//    val message1 = keypair2.decrypt(encryptedSum)
//    println(message1)
//    val message2 = keypair1.decrypt(EncryptedNumber(paillierContext2, message1.decodeBigInteger(), 0)).decodeDouble()
//    println(message2)
//    keyPairGenerator.initialize(2048, CryptographyUtils.secureRandom)
//    return keyPairGenerator.generateKeyPair()
//    val votingStateContract = context.getBean(VotingStateManagerService::class.java).contract
//    val bigNumbersContract = context.getBean(BigNumbersManagerService::class.java).contract
//    println("BIG NUMBERS ADDRESS: ${bigNumbersContract.contractAddress}")
//    println("START")
//    votingStateContract.clearLogs().send()
//    println("CLEARED")
//    votingStateContract.testBigNumbers().send()
//    printLogger(context)
//    println("END")

//    verifyRingSignatureOnChain(context)
//    printLogger(context)
}

private fun printLogger(context: ConfigurableApplicationContext){
    println("START LOGS ==============================")
    val votingStateContract = context.getBean(VotingStateManagerService::class.java).contract
    val logs = votingStateContract.getLogs().send()
    logs.forEach{println(it)}
    println("END LOGS ==============================")
}

//private fun verifyRingSignatureOnChain(context: ConfigurableApplicationContext){
//    val votingStateContract = context.getBean(VotingStateManagerService::class.java).contract
//    votingStateContract.clearLogs().send()
//    println("Cleared logs")
//    val newVote = generateVote()
////    votingStateContract.addVote(newVote).send()
//    votingStateContract.setVote(newVote).send()
//    println("Vote added")
//    val vote = votingStateContract.votes(BigInteger.ZERO).send()
//    println("Vote: $vote")
//
//    println("tag: ${vote.tag}")
//    println("ringValues: ${vote.ringValues().ringValues.joinToString(" ")}")
//    println("startValue: ${vote.component2().startValue}")
//    println("voteContent: ${vote.component1()}")
//    println("_______________________")
//
//    val hash = votingStateContract.hash().send()
//    println(hash)
//}

//private fun generateVote(): Vote {
//    val voteContent = "Janek"
//    val ringSignature = generateRingSignature(voteContent)
//    return Vote(voteContent, ringSignature)
//}

//private fun generateRingSignature(voteContent:String): VotingState.RingSignature {
//    val encoder = Base64.getEncoder()
//    val keyPair = generateRsaKeyPair()
//    val publicKeys = List(2){generateRsaKeyPair().public}
//    val signature = RingSignature.create(voteContent, keyPair, publicKeys)
//    signature.verify(voteContent)
//    return VotingState.RingSignature(
//        encoder.encodeToString(signature.startValue.toByteArray()),
//        signature.ringValues.map { encoder.encodeToString(it.toByteArray())},
//        encoder.encodeToString(signature.tag.toByteArray())
//    )
//}

private fun keyPairs() {
    val keyPair = generateRsaKeyPair()
    val publicKey = keyPair.public
    val privateKey = keyPair.private
    println("Public key: $publicKey")
    println("Private key: $privateKey")

    val secret: String = "DUPA_SŁONIA"
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, privateKey)
    val ciperText = cipher.doFinal(secret.toByteArray())
    println("Ciper text: ${String(ciperText)}")
    val decipher = Cipher.getInstance("RSA")
    decipher.init(Cipher.DECRYPT_MODE, publicKey)
    val plainText = decipher.doFinal(ciperText)
    println("Plain text: ${String(plainText)}")
}

fun generateRsaKeyPair(): KeyPair {
    val secureRandom = SecureRandom()
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(2048, secureRandom)
    return keyPairGenerator.generateKeyPair()
}


private fun deplyContracts() {


//    val walletFile = createWalletFile()
//    println("Wallet file name: $walletFile")

    val web3j = Web3j.build(HttpService("http://localhost:7545"))
//    val credentials: Credentials = WalletUtils.loadCredentials(
//        "c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3",
//        walletFile)
//    val credentials = Credentials.create("0x76b9e5dca270846e99ed7cf58a0eeb776cc2ee0f87101acd22350d1e633fbcde")
    val credentials = Credentials.create("0x76b9e5dca270846e99ed7cf58a0eeb776cc2ee0f87101acd22350d1e633fbcde")
    val deployContract = VotingState.deploy(
        web3j,
        credentials,
        StaticGasProvider(BigInteger.valueOf(766308394), BigInteger.valueOf(6721975L)),
//        DefaultGasProvider(),
    )

    println("DEPLOYING")
//    val contract = deployContract.send()
    val contract = VotingState.load(
        "0xe4f2024021cc0d11e8c410cc47cc4f3fd6046487",
        web3j,
        credentials,
        StaticGasProvider(BigInteger.valueOf(766308394), BigInteger.valueOf(6721975L))
    )
    println("Contract address: " + contract.contractAddress)

//    contract.addVote(Vote("Marcin")).send()
//    contract.addVote(Vote("Jan")).send()
//    contract.addVote(Vote("Basia")).send()

//    println(contract.votes(BigInteger.valueOf(0)).send())
//    println(contract.votes(BigInteger.valueOf(1)).send())
//    println(contract.votes(BigInteger.valueOf(2)).send())



//    println("GET " + contract.get().send())
//    println("SET " + contract.set(BigInteger.ONE).send())
//    println("GET " + contract.get().send())
//    val contractAddress: String = contract.getContractAddress()
//    val transactionReceipt = Transfer.sendFunds(
//        web3j, credentials, "0x<address>|<ensName>",
//        web3j, credentials, "0x6B9f1555035D4662589bd45BCc187114FBEd5A04",
//        BigDecimal.valueOf(1.0), Convert.Unit.ETHER
//    ).send()

//    println(transactionReceipt)
//    println(contract)

    val gasProvider = StaticGasProvider(BigInteger.valueOf(766308394), BigInteger.valueOf(6721975L))
//    val storage = SimpleStorage.load("0xe886bcf7d4e5fbb5202c12a9ac30a8cc43ce7c5f", web3j, credentials, gasProvider)

//    println("IS VALID " + storage.isValid)

//    val setResult = storage.setAge(BigInteger.valueOf(20)).send()


//    println("Set result: $setResult")

//    val getResult = storage.getAge().send()
//    println("Get result: $getResult")

//    val setResult = storage.set(BigInteger.valueOf(1)).send()
//    println("Set result: $setResult")
//    val getResult =  storage.get().send()
//    println("Get result: $getResult")
}

private fun createWalletFile(): File {
    val resourcesPath = Path.of(object {}.javaClass.classLoader.getResource("")!!.toURI())
    val walletFilesDir = Files.createDirectories(resourcesPath.resolve("walletFiles"))
    val walletName = WalletUtils.generateNewWalletFile(
        "c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3",
        walletFilesDir.toFile()
    )
    return walletFilesDir.resolve(walletName).toFile()
}
