#!/bin/bash
TOKEN=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@hypermall.vn","password":"123456"}' | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['data']['accessToken'])")
echo "Token: ${#TOKEN} chars"
RESP=$(curl -s -m 35 -X POST "http://localhost:8080/api/ai/chat" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"message":"Tim son moi mau do gia re"}')
echo "Raw: $RESP"
