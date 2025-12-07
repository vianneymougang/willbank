@echo off
chcp 65001 >nul
echo ========================================
echo   Tests Service Transaction - WillBank
echo ========================================
echo.

set BASE_URL=http://localhost:8082/api/transactions

echo [1/7] Test : Créer un dépôt...
curl -X POST %BASE_URL%/depot ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"amount\": 1000.00, \"type\": \"DEPOT\", \"description\": \"Salaire\"}"
echo.
echo.

timeout /t 2 /nobreak >nul

echo [2/7] Test : Créer un autre dépôt...
curl -X POST %BASE_URL%/depot ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"amount\": 500.00, \"type\": \"DEPOT\", \"description\": \"Prime\"}"
echo.
echo.

timeout /t 2 /nobreak >nul

echo [3/7] Test : Consulter l'historique...
curl -X GET %BASE_URL%/history/ACC001
echo.
echo.

timeout /t 2 /nobreak >nul

echo [4/7] Test : Recherche par type DEPOT...
curl -X GET "%BASE_URL%/search?accountId=ACC001&type=DEPOT"
echo.
echo.

timeout /t 2 /nobreak >nul

echo [5/7] Test : Recherche par statut COMPLETED...
curl -X GET "%BASE_URL%/search?accountId=ACC001&status=COMPLETED"
echo.
echo.

timeout /t 2 /nobreak >nul

echo [6/7] Test : Erreur - Montant négatif...
curl -X POST %BASE_URL%/depot ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"amount\": -100.00, \"type\": \"DEPOT\"}"
echo.
echo.

timeout /t 2 /nobreak >nul

echo [7/7] Test : Erreur - Virement vers soi-même...
curl -X POST %BASE_URL%/virement-interne ^
  -H "Content-Type: application/json" ^
  -d "{\"accountIdSource\": \"ACC001\", \"accountIdDestination\": \"ACC001\", \"amount\": 100.00, \"type\": \"VIREMENT_INTERNE\"}"
echo.
echo.

echo ========================================
echo   Tests terminés !
echo ========================================
echo.
echo Pour voir les données dans PostgreSQL :
echo   psql -U postgres -d willbank_transactions
echo   SELECT * FROM transactions;
echo.
echo Pour voir les événements dans RabbitMQ :
echo   http://localhost:15672
echo.
pause
