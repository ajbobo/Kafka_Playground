#!/bin/bash

aws_profile=$1

aws configure export-credentials --profile $aws_profile > creds.json

export AWS_ACCESS_KEY_ID=$(jq -r '.AccessKeyId' creds.json)
export AWS_SECRET_ACCESS_KEY=$(jq -r '.SecretAccessKey' creds.json)
export AWS_SESSION_TOKEN=$(jq -r '.SessionToken' creds.json)

echo "Environment variables set for profile $aws_profile"