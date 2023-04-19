# Get AWS username (based on default profile)
$user = (aws sts get-caller-identity) | ConvertFrom-Json
$username = $user.Arn.Substring($user.Arn.LastIndexOf('/') + 1)

# Get session credentials
$code = Read-Host "Enter MFA code for user $username"
$creds = (aws sts assume-role `
    --role-arn "arn:aws:iam::300813158921:role/GroupAccess-Developers-Orchestration" `
    --role-session-name "kafka-ui-$username" `
    --serial-number "arn:aws:iam::736763050260:mfa/$username" `
    --token-code $code) | ConvertFrom-Json -Depth 3

# Store creds in temporary environment variables
$env:AWS_ACCESS_KEY = $creds.Credentials.AccessKeyId
$env:AWS_SECRET_ACCESS_KEY = $creds.Credentials.SecretAccessKey
$env:AWS_SESSION_TOKEN = $creds.Credentials.SessionToken

# Start up the UI container
docker compose up -d