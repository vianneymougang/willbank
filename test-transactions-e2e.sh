#!/bin/bash
# E2E Transaction Tests Script
set -e

echo "🧪 Starting E2E Transaction Tests..."
echo "===================================="

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Admin Login
echo -e "\n${YELLOW}1. Admin Login...${NC}"
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@willbank.com", "password": "admin123"}' \
  | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ADMIN_TOKEN" ]; then
  echo -e "${RED}❌ Admin login failed${NC}"
  exit 1
fi
echo -e "${GREEN}✅ Admin logged in${NC}"

# Customer Login  
echo -e "\n${YELLOW}2. Customer Login...${NC}"
CUSTOMER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "student@example.com", "password": "VickySecure123!"}' \
  | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$CUSTOMER_TOKEN" ]; then
  echo -e "${RED}❌ Customer login failed${NC}"
  exit 1
fi
echo -e "${GREEN}✅ Customer logged in${NC}"

# Get Customer Accounts
echo -e "\n${YELLOW}3. Getting Customer Accounts...${NC}"
ACCOUNTS=$(curl -s http://localhost:8080/api/accounts/customer/1 \
  -H "Authorization: Bearer $CUSTOMER_TOKEN")

echo "$ACCOUNTS" | head -30
ACCOUNT_ID=$(echo "$ACCOUNTS" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -z "$ACCOUNT_ID" ]; then
  echo -e "${RED}❌ No account found${NC}"
  exit 1
fi
echo -e "${GREEN}✅ Account ID: $ACCOUNT_ID${NC}"

# Test 1: DEPOSIT
echo -e "\n${YELLOW}4. TEST: Deposit 500$...${NC}"
DEPOSIT_RESULT=$(curl -s -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"accountId\": $ACCOUNT_ID,
    \"amount\": 500.00,
    \"type\": \"DEPOSIT\",
    \"description\": \"Test deposit from E2E script\"
  }")

echo "$DEPOSIT_RESULT"
if echo "$DEPOSIT_RESULT" | grep -q "COMPLETED"; then
  echo -e "${GREEN}✅ Deposit successful${NC}"
else
  echo -e "${RED}❌ Deposit failed${NC}"
fi

# Wait for async processing
echo "⏳ Waiting 3s for email processing..."
sleep 3

# Test 2: WITHDRAWAL  
echo -e "\n${YELLOW}5. TEST: Withdrawal 100$...${NC}"
WITHDRAWAL_RESULT=$(curl -s -X POST http://localhost:8080/api/transactions/withdraw \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"accountId\": $ACCOUNT_ID,
    \"amount\": 100.00,
    \"type\": \"WITHDRAWAL\",
    \"description\": \"Test withdrawal from E2E script\"
  }")

echo "$WITHDRAWAL_RESULT"
if echo "$WITHDRAWAL_RESULT" | grep -q "COMPLETED"; then
  echo -e "${GREEN}✅ Withdrawal successful${NC}"
else
  echo -e "${RED}❌ Withdrawal failed${NC}"
fi

sleep 3

# Test 3: LARGE TRANSACTION (> 10000$)
echo -e "\n${YELLOW}6. TEST: Large Deposit 15000$ (Alert expected)...${NC}"
LARGE_RESULT=$(curl -s -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer $CUSTOMER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"accountId\": $ACCOUNT_ID,
    \"amount\": 15000.00,
    \"type\": \"DEPOSIT\",
    \"description\": \"Large deposit test - should trigger alert\"
  }")

echo "$LARGE_RESULT"
if echo "$LARGE_RESULT" | grep -q "COMPLETED"; then
  echo -e "${GREEN}✅ Large deposit successful${NC}"
else
  echo -e "${RED}❌ Large deposit failed${NC}"
fi

sleep 3

# Verification
echo -e "\n${YELLOW}7. Verifications...${NC}"
echo "📧 Check MailTrap: http://localhost:1080"
echo "🐰 Check RabbitMQ: http://localhost:15672 (guest/guest)"
echo "🗄️  Check MongoDB: http://localhost:8087 (admin/admin123)"

echo -e "\n${GREEN}✅ E2E Tests Completed!${NC}"
echo "===================================="
