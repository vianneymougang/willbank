@echo off
REM Script de test complet de tous les endpoints (Windows)
REM Usage: test-all-endpoints.bat

set BASE_URL=http://localhost:8082
set ACCOUNT1=ACC001
set ACCOUNT2=ACC002

echo ==========================================
echo Tests du Microservice Transaction WillBank
echo ==========================================
echo.

REM Test 1: Créer un dépôt
echo Test 1: Créer un dépôt
curl -X POST "%BASE_URL%/api/transactions/depot" ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"%ACCOUNT1%\", \"amount\": 5000.00, \"type\": \"DEPOT\", \"description\": \"Dépôt initial - Salaire\"}"
echo.
timeout /t 1 /nobreak >nul

REM Test 2: Créer un autre dépôt
echo Test 2: Créer un second dépôt
curl -X POST "%BASE_URL%/api/transactions/depot" ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"%ACCOUNT1%\", \"amount\": 1500.00, \"type\": \"DEPOT\", \"description\": \"Prime\"}"
echo.
timeout /t 1 /nobreak >nul

REM Test 3: Créer un virement interne
echo Test 3: Créer un virement interne
curl -X POST "%BASE_URL%/api/transactions/virement-interne" ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"%ACCOUNT1%\", \"accountIdDestination\": \"%ACCOUNT2%\", \"amount\": 1000.00, \"type\": \"VIREMENT_INTERNE\", \"description\": \"Virement vers compte épargne\"}"
echo.
timeout /t 1 /nobreak >nul

REM Test 4: Créer un retrait
echo Test 4: Créer un retrait
curl -X POST "%BASE_URL%/api/transactions/retrait" ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"%ACCOUNT1%\", \"amount\": 200.00, \"type\": \"RETRAIT\", \"description\": \"Retrait DAB\"}"
echo.
timeout /t 1 /nobreak >nul

REM Test 5: Consulter l'historique
echo Test 5: Consulter l'historique des transactions
curl -X GET "%BASE_URL%/api/transactions/history/%ACCOUNT1%"
echo.
timeout /t 1 /nobreak >nul

REM Test 6: Recherche par type DEPOT
echo Test 6: Recherche par type DEPOT
curl -X GET "%BASE_URL%/api/transactions/search?accountId=%ACCOUNT1%&type=DEPOT"
echo.
timeout /t 1 /nobreak >nul

REM Test 7: Recherche par statut COMPLETED
echo Test 7: Recherche par statut COMPLETED
curl -X GET "%BASE_URL%/api/transactions/search?accountId=%ACCOUNT1%&status=COMPLETED"
echo.
timeout /t 1 /nobreak >nul

REM Test 8: Recherche par type et statut
echo Test 8: Recherche par type VIREMENT_INTERNE et statut COMPLETED
curl -X GET "%BASE_URL%/api/transactions/search?accountId=%ACCOUNT1%&type=VIREMENT_INTERNE&status=COMPLETED"
echo.
timeout /t 1 /nobreak >nul

REM Test 9: Recherche par période
echo Test 9: Recherche par période
curl -X GET "%BASE_URL%/api/transactions/date-range?accountId=%ACCOUNT1%&startDate=2024-12-01T00:00:00&endDate=2024-12-31T23:59:59"
echo.
timeout /t 1 /nobreak >nul

REM Test 10: Validation - Montant négatif (doit échouer)
echo Test 10: Validation - Montant négatif (doit échouer)
curl -X POST "%BASE_URL%/api/transactions/depot" ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"%ACCOUNT1%\", \"amount\": -100.00, \"type\": \"DEPOT\", \"description\": \"Test montant négatif\"}"
echo.
timeout /t 1 /nobreak >nul

REM Test 11: Validation - Compte source vide (doit échouer)
echo Test 11: Validation - Compte source vide (doit échouer)
curl -X POST "%BASE_URL%/api/transactions/depot" ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"\", \"amount\": 100.00, \"type\": \"DEPOT\", \"description\": \"Test compte vide\"}"
echo.
timeout /t 1 /nobreak >nul

REM Test 12: Validation - Virement vers le même compte (doit échouer)
echo Test 12: Validation - Virement vers le même compte (doit échouer)
curl -X POST "%BASE_URL%/api/transactions/virement-interne" ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"%ACCOUNT1%\", \"accountIdDestination\": \"%ACCOUNT1%\", \"amount\": 100.00, \"type\": \"VIREMENT_INTERNE\", \"description\": \"Test même compte\"}"
echo.

echo.
echo ==========================================
echo Tests terminés!
echo ==========================================
pause
