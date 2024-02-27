#!/bin/bash

SCRIPT_DIR="$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)"
cd "$SCRIPT_DIR"

if [ -z "$1" ] || ([ "$1" != "dev" ] && [ "$1" != "demo" ]); then
    echo "Usage: ./run-be-docker.sh <ENV>"
    echo "ENV := dev | demo"
    exit 1
fi

if [ "$1" = "demo" ] && [ -z "$2" ]; then
  echo "Usage: ./run-be-docker.sh demo <SMTP_PASSWORD>"
  exit 2
fi

if [ "$1" = "demo" ]; then
  export SMTP_PASSWORD="$2"
  ./clean.sh all
else
  ./clean.sh
fi

BE_DIR="$SCRIPT_DIR/.."
cd "$BE_DIR"

BE_IMAGE_NAME="healthblocks/backend-ums"
export BE_IMAGE_NAME

./gradlew bootBuildImage --imageName="$BE_IMAGE_NAME"

cd "$SCRIPT_DIR"

docker-compose --profile "$1" -p healthblocks-ums up -d