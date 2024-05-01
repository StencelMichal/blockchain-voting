#--no-cache

echo "Building bootnode container"
docker build -t blockchain-evoting/bootnode --progress=plain -f bootnode/Dockerfile .
echo "bootnode build complete"

echo "Building sealer container"
docker build -t blockchain-evoting/sealer --progress=plain -f sealer/Dockerfile .
echo "sealer build complete"

echo "Building voter container"
docker build -t blockchain-evoting/voter --progress=plain -f voter/Dockerfile .
echo "voter build complete"

