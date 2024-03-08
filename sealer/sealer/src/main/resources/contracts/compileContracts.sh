#!/bin/bash

for file in *.sol; do
  if [ -f "$file" ]; then

    echo "Compiling file: $file"
    solc $file --overwrite --bin --abi --optimize -o ./compiled

    filename="${file%.*}"

    echo "Generating java classes for: $file"
    web3j generate solidity \
      --abiFile=./compiled/$filename.abi \
      --binFile=./compiled/$filename.bin \
      --outputDir=../../java \
      --package=com.stencel.evoting.sealer

    echo "-----------------------------------------"
  fi
done
