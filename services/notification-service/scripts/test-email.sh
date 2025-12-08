#!/bin/bash

# Script de test pour l'envoi d'email

echo "=========================================="
echo "Test d'envoi d'Email"
echo "=========================================="

# Demander l'email destinataire
read -p "Email destinataire (ou Entrée pour test@example.com): " EMAIL
EMAIL=${EMAIL:-test@example.com}

echo ""
echo "Envoi d'un email de test à: $EMAIL"

# Envoyer la requête
curl -X POST http://localhost:8082/api/notifications/history/send \
  -H "Content-Type: application/json" \
  -d "{
    \"recipient\": \"$EMAIL\",
    \"subject\": \"Test Email - Microservice Notifications\",
    \"content\": \"<html><body><h1>Test Email</h1><p>Ceci est un email de test envoyé depuis le microservice de notifications.</p><p>Date: $(date)</p><table border='1' cellpadding='10'><tr><td><strong>Service:</strong></td><td>Notification Service</td></tr><tr><td><strong>Type:</strong></td><td>Email Test</td></tr><tr><td><strong>Status:</strong></td><td>✅ Opérationnel</td></tr></table><p>Merci!</p></body></html>\",
    \"type\": \"EMAIL\",
    \"userId\": \"test-user\",
    \"transactionId\": \"TEST-$(date +%s)\"
  }"

echo ""
echo ""
echo "✅ Requête envoyée!"
echo ""
echo "Vérifiez:"
echo "- Votre boîte email: $EMAIL"
echo "- Les logs du service: docker-compose logs -f notification-service"
echo "- L'historique: curl http://localhost:8082/api/notifications/history"
