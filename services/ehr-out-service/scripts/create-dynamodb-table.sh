#!/bin/bash
set -e
echo Creating a table for test in dynamodb-local...
cd "$(dirname "$0")"
aws --region eu-west-2 --endpoint=http://dynamodb-local:8000 dynamodb create-table --cli-input-json file://local-test-db-scheme.json --no-cli-page
set +e