#!/usr/bin/env bash

SCRIPT_PATH="$(cd -- "$(dirname "$0")" >/dev/null 2>&1 && pwd -P)"
cd "$SCRIPT_PATH"

if [ -z "$1" ]; then
    PROTOS_DIR_PATH="../../protos"
    if [ ! -d "$PROTOS_DIR_PATH" ]; then
        echo "Error: No argument provided and $PROTOS_DIR_PATH not present (repo)"
        echo "Usage: ./copy-protos.sh [<PROTOS_DIR_PATH>]"
        exit 1
    fi
else
    if [[ ! "$1" =~ ^/ ]]; then
        echo "Error: Provided argument is not an absolute path"
        echo "Usage: ./copy-protos.sh [<PROTOS_DIR_PATH>]"
        exit 2
    fi
    PROTOS_DIR_PATH="$1"
fi

cd "$PROTOS_DIR_PATH"

if [ ! -d "./bindings/java/build/libs/" ]; then
  if ! docker info >/dev/null 2>&1; then
    error "Docker is not running"
    exit 4
  fi
  ./build.sh
fi

cd "$SCRIPT_PATH"

function copy_file {
  local source_file="$1"
  local destination="$2"

  if [ -f "$source_file" ]; then
    cp "$source_file" "$destination"
  else
    echo "Error: File $source_file not found"
    exit 5
  fi
}

if [ -d "../libs" ]; then
  rm -rf "../libs"
fi

cp -R "$PROTOS_DIR_PATH/bindings/java/build/libs/" "../libs"