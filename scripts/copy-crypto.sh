#!/usr/bin/env bash

cd -- "$(dirname "$0")"

if [ -z "$1" ]; then
    PEER_ORGANIZATIONS_CRYPTO_DIR_PATH="../../tools/fabric/.generated/crypto/peerOrganizations/"
    if [ ! -d "$PEER_ORGANIZATIONS_CRYPTO_DIR_PATH" ]; then
        echo "Error: No argument provided and $PEER_ORGANIZATIONS_CRYPTO_DIR_PATH not present (repo)"
        echo "Usage: ./copy-crypto.sh [<PEER_ORGANIZATIONS_CRYPTO_DIR_PATH>]"
        exit 1
    fi
else
    if [[ ! "$1" =~ ^/ ]]; then
        echo "Error: Provided argument is not an absolute path"
        echo "Usage: ./copy-crypto.sh [<PEER_ORGANIZATIONS_CRYPTO_DIR_PATH>]"
        exit 2
    fi
    PEER_ORGANIZATIONS_CRYPTO_DIR_PATH="$1"
fi

function copy_file {
  local source_file="$1"
  local destination="$2"

  if [ -f "$source_file" ]; then
    cp "$source_file" "$destination"
  else
    echo "Error: File $source_file not found"
    exit 3
  fi
}

function copy_dir {
  local source="$1"
  local destination="$2"

  if [ -d "$source" ]; then
    cp -R "$source" "$destination"
  else
    echo "Error: Directory $source not found"
    exit 4
  fi
}

function copy_crypto() {
    local domain="$1"

    if [ -d "$PEER_ORGANIZATIONS_CRYPTO_DIR_PATH/$domain" ]; then
      mkdir -p "../crypto/$domain/msp" "../crypto/$domain/tls"
      copy_dir "$PEER_ORGANIZATIONS_CRYPTO_DIR_PATH/$domain/users/User1@$domain/msp/keystore" "../crypto/$domain/msp"
      copy_dir "$PEER_ORGANIZATIONS_CRYPTO_DIR_PATH/$domain/users/User1@$domain/msp/signcerts" "../crypto/$domain/msp"
      copy_file "$PEER_ORGANIZATIONS_CRYPTO_DIR_PATH/$domain/users/User1@$domain/tls/ca.crt" "../crypto/$domain/tls"
    fi
}

copy_crypto "medtechchain.nl"