#!/bin/bash

folders=(
    "docker/bootnode/data"
    "docker/sealer/sealer-1/data"
    "docker/sealer/sealer-2/data"
    "docker/sealer/sealer-3/data"
)

for folder in "${folders[@]}"; do
    cd "$folder" || exit
    for file in *; do
        if [[ $file != "key" && $file != "key.pub" ]]; then
            rm -rf "$file"
        fi
    done
    cd -
done

