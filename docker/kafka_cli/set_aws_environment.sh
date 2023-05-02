#!/bin/bash

aws_profile=$1

aws configure export-credentials --profile $aws_profile > creds.json

export AWS_ACCESS_KEY_ID=$(jq -r '.AccessKeyId' creds.json)
export AWS_SECRET_ACCESS_KEY=$(jq -r '.SecretAccessKey' creds.json)
export AWS_SESSION_TOKEN=$(jq -r '.SessionToken' creds.json)

if [ $AWS_SESSION_TOKEN = "null" ]
then
  echo "Need to get a session token"
  aws sts get-session-token --profile $aws_profile > creds.json
  export AWS_ACCESS_KEY_ID=$(jq -r '.Credentials.AccessKeyId' creds.json)
  export AWS_SECRET_ACCESS_KEY=$(jq -r '.Credentials.SecretAccessKey' creds.json)
  export AWS_SESSION_TOKEN=$(jq -r '.Credentials.SessionToken' creds.json)
fi

echo "Environment variables set for profile $aws_profile"