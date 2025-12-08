#!/bin/bash

# Script de test pour l'envoi de notification push FCM

echo "=========================================="
echo "Test d'envoi de Notification Push (FCM)"
echo "=========================================="

# Demander le token FCM
read -p "Token FCM (obligatoire): " FCM_TOKEN

if [ -z "$FCM_TOKEN" ]; then
    echo "❌ Le token FCM est obligatoire"
    exit 1
fi

echo ""
echo "Envoi d'une notification push..."

# Envoyer la requête
curl -X POST http://localhost:8082/api/notifications/history/send \
  -H "Content-Type: application/json" \
  -d "{
    \"recipient\": \"user@example.com\",
    \"subject\": \"Test Push Notification\",
    \"content\": \"Ceci est une notification push de test envoyée depuis le microservice.\",
    \"type\": \"PUSH\",
    \"userId\": \"test-user\",
    \"transactionId\": \"TEST-$(date +%s)\",
    \"fcmToken\": \"$FCM_TOKEN\"
  }"

echo ""
echo ""
echo "✅ Requête envoyée!"
echo ""
echo "Vérifiez:"
echo "- Votre appareil mobile"
echo "- Les logs du service: docker-compose logs -f notification-service"
echo "- La console Firebase: https://console.firebase.google.com/"
