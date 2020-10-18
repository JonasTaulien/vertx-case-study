#!/usr/bin/env bash
BOTH=/tmp/jwt.pem
PRIVATE=/tmp/jwt.private.pem
PUBLIC=/tmp/jwt.public.pem

openssl genrsa -out "${BOTH}" 2048 &>/dev/null
openssl pkcs8 -topk8 -inform PEM -in "${BOTH}" -out "${PRIVATE}" -nocrypt &>/dev/null
openssl rsa -in "${PRIVATE}" -outform PEM -pubout -out "${PUBLIC}" &>/dev/null

echo "PrivateKey"
(tr -d '\n' < "${PRIVATE}") | sed 's/-----BEGIN PRIVATE KEY-----//g' | sed 's/-----END PRIVATE KEY-----//g'

echo ""
echo "PublicKey:"
tr -d '\n' < "${PUBLIC}" | sed 's/-----BEGIN PUBLIC KEY-----//g' | sed 's/-----END PUBLIC KEY-----//g'
