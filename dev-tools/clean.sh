#!/bin/bash

SCRIPT_DIR="$(cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P)"
cd "$SCRIPT_DIR"

if [ "$1" = "all" ]; then
  docker-compose -p medtechchain-ums down -v
else
  docker-compose -p medtechchain-ums down
fi