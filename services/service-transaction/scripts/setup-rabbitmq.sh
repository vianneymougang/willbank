#!/bin/bash

# Script de configuration RabbitMQ
# Ce script crée les exchanges, queues et bindings nécessaires

echo "Configuration de RabbitMQ pour WillBank Transaction Service"
echo "============================================================"

# Variables
RABBITMQ_HOST="localhost"
RABBITMQ_PORT="15672"
RABBITMQ_USER="guest"
RABBITMQ_PASS="guest"
VHOST="/"

# Créer l'exchange
echo "Création de l'exchange 'transaction.exchange'..."
curl -i -u $RABBITMQ_USER:$RABBITMQ_PASS -H "content-type:application/json" \
  -XPUT http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/exchanges/$VHOST/transaction.exchange \
  -d '{"type":"topic","durable":true}'

echo ""

# Créer la queue
echo "Création de la queue 'transaction.queue'..."
curl -i -u $RABBITMQ_USER:$RABBITMQ_PASS -H "content-type:application/json" \
  -XPUT http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/queues/$VHOST/transaction.queue \
  -d '{"durable":true}'

echo ""

# Créer le binding
echo "Création du binding avec routing key 'transaction.created'..."
curl -i -u $RABBITMQ_USER:$RABBITMQ_PASS -H "content-type:application/json" \
  -XPOST http://$RABBITMQ_HOST:$RABBITMQ_PORT/api/bindings/$VHOST/e/transaction.exchange/q/transaction.queue \
  -d '{"routing_key":"transaction.created"}'

echo ""
echo "Configuration terminée!"
echo ""
echo "Vérifiez sur http://$RABBITMQ_HOST:$RABBITMQ_PORT"
echo "Username: $RABBITMQ_USER"
echo "Password: $RABBITMQ_PASS"
