#!/bin/bash

SCRIPT_DIR="$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)"
cd "$SCRIPT_DIR"

BE_IMAGE_NAME="medtechchain/backend-ums"
export BE_IMAGE_NAME

docker-compose --profile deps -p medtechchain-ums up -d