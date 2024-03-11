#!/bin/bash

cd -- "$(dirname "$0")"

if [ -z "$1" ] || ([ "$1" != "dev" ] && [ "$1" != "demo" ]); then
    echo "Usage: ./run-be-docker.sh <ENV>"
    echo "ENV := dev | demo"
    exit 1
fi

if [ "$1" = "demo" ] && [ -z "$2" ]; then
  echo "Usage: ./run-be-docker.sh demo <SMTP_PASSWORD>"
  exit 2
fi

source .env
export BE_IMAGE_NAME
export LOCAL_USER_CRYPTO_PATH

if [ ! -d "$FABRIC_GEN_USER_CRYPTO_PATH" ]; then
    echo ">>> ERROR: Infrastructure crypto material not found. Make sure to run the fabric infrastructure before backend/"
    exit 3
fi

rm -rf "$LOCAL_USER_CRYPTO_PATH"
mkdir -p "$LOCAL_USER_CRYPTO_PATH"
cp -RT "$FABRIC_GEN_USER_CRYPTO_PATH" "$LOCAL_USER_CRYPTO_PATH"

SMTP_PASSWORD="$2"
export SMTP_PASSWORD


cd ..

./gradlew bootBuildImage --imageName="$BE_IMAGE_NAME"

if [ $? -ne 0 ]; then
    echo ">>> ERROR: backend build failed with status $?"
    exit 4
fi

cd ./dev-tools

if [ ! "$(docker network ls --format "{{.Name}}" | grep "^$NETWORK")" ]; then
      docker network create --driver bridge "$NETWORK"
fi

docker-compose --profile "$1" -p medtechchain up -d