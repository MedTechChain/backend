#!/bin/bash

cd -- "$(dirname "$0")"

source .env

export BE_IMAGE_NAME

if [ ! "$(docker network ls --format "{{.Name}}" | grep "^$NETWORK")" ]; then
      docker network create --driver bridge "$NETWORK"
fi

docker-compose --profile deps -p medtechchain-ums up -d