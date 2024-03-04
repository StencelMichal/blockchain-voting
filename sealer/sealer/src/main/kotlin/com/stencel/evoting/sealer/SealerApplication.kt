package com.stencel.evoting.sealer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.Transfer
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.gas.StaticGasProvider
import org.web3j.utils.Convert
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path


@SpringBootApplication
class SealerApplication

fun main(args: Array<String>) {
    runApplication<SealerApplication>(*args)

    println(SimpleStorage.BINARY)

//    val walletFile = createWalletFile()
//    println("Wallet file name: $walletFile")

    val web3j = Web3j.build(HttpService("http://localhost:8545"))
//    val credentials: Credentials = WalletUtils.loadCredentials(
//        "c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3",
//        walletFile)
//    val credentials = Credentials.create("0x76b9e5dca270846e99ed7cf58a0eeb776cc2ee0f87101acd22350d1e633fbcde")
    val credentials = Credentials.create("c87509a1c067bbde78beb793e6fa76530b6382a4c0241e5e4a9ec0a0f44dc0d3")
    val deployContract = SimpleStorage.deploy(
        web3j,
        credentials,
        StaticGasProvider(BigInteger.valueOf(766308394), BigInteger.valueOf(6721975L)),
//        DefaultGasProvider(),
    )

    println("DEPLOYING")
    val contract = deployContract.send()
    println("Contract address: " + contract.contractAddress)
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
