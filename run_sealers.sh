sealers_amount=3
cd docker/sealer || exit
for ((sealer_no = 1; sealer_no <= sealers_amount; sealer_no++)); do
  echo "Booting sealer $sealer_no"
  docker compose \
    --file docker-compose.yaml \
    --env-file sealer-"$sealer_no"/.env \
    --project-name sealer-"$sealer_no" \
    up --detach
  echo "Sealer $sealer_no booted"
done
cd - || exit
