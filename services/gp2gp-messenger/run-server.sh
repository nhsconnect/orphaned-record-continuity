#!/bin/bash

# This script executes on docker container start.
echo "Loading API Keys"
source ./scripts/load-api-keys.sh

echo "Starting the application"
set -e
exec node server.js
