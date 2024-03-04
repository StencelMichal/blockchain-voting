 solc SimpleStorage.sol --overwrite --bin --abi --optimize -o ./compiled

 web3j generate solidity \
   --abiFile=./compiled/SimpleStorage.abi \
   --binFile=./compiled/SimpleStorage.bin \
   --outputDir=../../java \
   --package=com.stencel.evoting.sealer