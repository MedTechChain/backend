#!/bin/bash

cd -- "$(dirname "$0")"

echo ">>> Stopping MedTechChain backend (IGNORE WARNINGS) <<<"

source .env

SMTP_PASSWORD=""
export SMTP_PASSWORD
export BE_IMAGE_NAME
export LOCAL_USER_CRYPTO_PATH

if [ "$1" = "all" ]; then
  docker-compose -p medtechchain down -v
else
  docker-compose -p medtechchain down
fi
