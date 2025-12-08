#!/bin/bash

# Script de test RabbitMQ pour le microservice de notifications

echo "=========================================="
echo "Test RabbitMQ - Microservice Notifications"
echo "=========================================="

# Vérifier si RabbitMQ est accessible
if ! curl -s http://localhost:15672 > /dev/null; then
    echo "❌ RabbitMQ n'est pas accessible sur http://localhost:15672"
    echo "Démarrez RabbitMQ avec: rabbitmq-server"
    exit 1
fi

echo "✅ RabbitMQ est accessible"

# Publier un événement de test
echo ""
echo "Publication d'un événement de transaction..."

curl -i -u guest:guest -H "content-type:application/json" \
  -X POST http://localhost:15672/api/exchanges/%2F/transaction.exchange/publish \
  -d '{
    "properties": {
      "content_type": "application/json",
      "delivery_mode": 2
    },
    "routing_key": "transaction.completed",
    "payload": "{\"transactionId\":\"TXN-TEST-001\",\"userId\":\"user123\",\"type\":\"DEPOSIT\",\"amount\":100.00,\"currency\":\"EUR\",\"status\":\"COMPLETED\",\"timestamp\":\"2024-01-15T10:30:00\",\"recipientEmail\":\"test@example.com\",\"recipientPhone\":\"+33612345678\",\"fcmToken\":null,\"metadata\":{\"source\":\"test-script\"}}",
    "payload_encoding": "string"
  }'

echo ""
echo ""
echo "✅ Événement publié!"
echo ""
echo "Vérifiez:"
echo "- Les logs du microservice"
echo "- L'interface RabbitMQ: http://localhost:15672"
echo "- Votre boîte email"
