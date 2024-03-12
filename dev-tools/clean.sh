#!/bin/bash

cd -- "$(dirname "$0")"

source .env

SMTP_PASSWORD=""
export SMTP_PASSWORD
export BE_IMAGE_NAME
export LOCAL_USER_CRYPTO_PATH

if [ "$1" = "all" ]; then
  docker-compose -p medtechchain-ums down -v
else
  docker-compose -p medtechchain-ums down
fi
