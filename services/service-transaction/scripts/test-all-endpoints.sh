#!/bin/bash

# Script de test complet de tous les endpoints
# Usage: ./test-all-endpoints.sh

BASE_URL="http://localhost:8082"
ACCOUNT1="ACC001"
ACCOUNT2="ACC002"

echo "=========================================="
echo "Tests du Microservice Transaction WillBank"
echo "=========================================="
echo ""

# Couleurs pour l'affichage
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Créer un dépôt
echo -e "${YELLOW}Test 1: Créer un dépôt${NC}"
curl -X POST "$BASE_URL/api/transactions/depot" \
  -H "Content-Type: application/json" \
  -d "{
    \"accountIdSource\": \"$ACCOUNT1\",
    \"amount\": 5000.00,
    \"type\": \"DEPOT\",
    \"description\": \"Dépôt initial - Salaire\"
  }" \
  -w "\nStatus: %{http_code}\n\n"

sleep 1

# Test 2: Créer un autre dépôt
echo -e "${YELLOW}Test 2: Créer un second dépôt${NC}"
curl -X POST "$BASE_URL/api/transactions/depot" \
  -H "Content-Type: application/json" \
  -d "{
    \"accountIdSource\": \"$ACCOUNT1\",
    \"amount\": 1500.00,
    \"type\": \"DEPOT\",
    \"description\": \"Prime\"
  }" \
  -w "\nStatus: %{http_code}\n\n"

sleep 1

# Test 3: Créer un virement interne
echo -e "${YELLOW}Test 3: Créer un virement interne${NC}"
curl -X POST "$BASE_URL/api/transactions/virement-interne" \
  -H "Content-Type: application/json" \
  -d "{
    \"accountIdSource\": \"$ACCOUNT1\",
    \"accountIdDestination\": \"$ACCOUNT2\",
    \"amount\": 1000.00,
    \"type\": \"VIREMENT_INTERNE\",
    \"description\": \"Virement vers compte épargne\"
  }" \
  -w "\nStatus: %{http_code}\n\n"

sleep 1

# Test 4: Créer un retrait
echo -e "${YELLOW}Test 4: Créer un retrait${NC}"
curl -X POST "$BASE_URL/api/transactions/retrait" \
  -H "Content-Type: application/json" \
  -d "{
    \"accountIdSource\": \"$ACCOUNT1\",
    \"amount\": 200.00,
    \"type\": \"RETRAIT\",
    \"description\": \"Retrait DAB\"
  }" \
  -w "\nStatus: %{http_code}\n\n"

sleep 1

# Test 5: Consulter l'historique
echo -e "${YELLOW}Test 5: Consulter l'historique des transactions${NC}"
curl -X GET "$BASE_URL/api/transactions/history/$ACCOUNT1" \
  -w "\nStatus: %{http_code}\n\n"

sleep 1

# Test 6: Recherche par type DEPOT
echo -e "${YELLOW}Test 6: Recherche par type DEPOT${NC}"
curl -X GET "$BASE_URL/api/transactions/search?accountId=$ACCOUNT1&type=DEPOT" \
  -w "\nStatus: %{http_code}\n\n"

sleep 1

# Test 7: Recherche par statut COMPLETED
echo -e "${YELLOW}Test 7: Recherche par statut COMPLETED${NC}"
curl -X GET "$BASE_URL/api/transactions/search?accountId=$ACCOUNT1&status=COMPLETED" \
  -w "\nStatus: %{http_code}\n\n"

sleep 1

# Test 8: Recherche par type et statut
echo -e "${YELLOW}Test 8: Recherche par type VIREMENT_INTERNE et statut COMPLETED${NC}"
curl -X GET "$BASE_URL/api/transactions/search?accountId=$ACCOUNT1&type=VIREMENT_INTERNE&status=COMPLETED" \
  -w "\nStatus: %{http_code}\n\n"

sleep 1

# Test 9: Recherche par période
echo -e "${YELLOW}Test 9: Recherche par période${NC}"
START_DATE="2024-12-01T00:00:00"
END_DATE="2024-12-31T23:59:59"
curl -X GET "$BASE_URL/api/transactions/date-range?accountId=$ACCOUNT1&startDate=$START_DATE&endDate=$END_DATE" \
  -w "\nStatus: %{http_code}\n\n"

sleep 1

# Test 10: Validation - Montant négatif (doit échouer)
echo -e "${YELLOW}Test 10: Validation - Montant négatif (doit échouer)${NC}"
curl -X POST "$BASE_URL/api/transactions/depot" \
  -H "Content-Type: application/json" \
  -d "{
    \"accountIdSource\": \"$ACCOUNT1\",
    \"amount\": -100.00,
    \"type\": \"DEPOT\",
    \"description\": \"Test montant négatif\"
  }" \
  -w "\nStatus: %{http_code}\n\n"

sleep 1

# Test 11: Validation - Compte source vide (doit échouer)
echo -e "${YELLOW}Test 11: Validation - Compte source vide (doit échouer)${NC}"
curl -X POST "$BASE_URL/api/transactions/depot" \
  -H "Content-Type: application/json" \
  -d "{
    \"accountIdSource\": \"\",
    \"amount\": 100.00,
    \"type\": \"DEPOT\",
    \"description\": \"Test compte vide\"
  }" \
  -w "\nStatus: %{http_code}\n\n"

sleep 1

# Test 12: Validation - Virement vers le même compte (doit échouer)
echo -e "${YELLOW}Test 12: Validation - Virement vers le même compte (doit échouer)${NC}"
curl -X POST "$BASE_URL/api/transactions/virement-interne" \
  -H "Content-Type: application/json" \
  -d "{
    \"accountIdSource\": \"$ACCOUNT1\",
    \"accountIdDestination\": \"$ACCOUNT1\",
    \"amount\": 100.00,
    \"type\": \"VIREMENT_INTERNE\",
    \"description\": \"Test même compte\"
  }" \
  -w "\nStatus: %{http_code}\n\n"

echo ""
echo -e "${GREEN}=========================================="
echo "Tests terminés!"
echo -e "==========================================${NC}"
