#!/usr/bin/env python3
"""
Script de test pour publier des événements de transaction sur RabbitMQ
"""

import pika
import json
import sys
from datetime import datetime
import random

def publish_transaction_event(event_type='DEPOSIT'):
    """Publie un événement de transaction sur RabbitMQ"""
    
    # Connexion à RabbitMQ
    connection = pika.BlockingConnection(
        pika.ConnectionParameters('localhost')
    )
    channel = connection.channel()
    
    # Créer l'exchange s'il n'existe pas
    channel.exchange_declare(
        exchange='transaction.exchange',
        exchange_type='topic',
        durable=True
    )
    
    # Générer un événement de test
    transaction_id = f"TXN-{datetime.now().strftime('%Y%m%d')}-{random.randint(1000, 9999)}"
    
    event = {
        "transactionId": transaction_id,
        "userId": "user123",
        "type": event_type,
        "amount": round(random.uniform(10, 1000), 2),
        "currency": "EUR",
        "status": "COMPLETED",
        "timestamp": datetime.now().isoformat(),
        "recipientEmail": "test@example.com",
        "recipientPhone": "+33612345678",
        "fcmToken": None,  # Mettre votre token FCM ici pour tester les push
        "metadata": {
            "source": "test-script",
            "ip": "127.0.0.1"
        }
    }
    
    # Publier l'événement
    routing_key = f"transaction.{event_type.lower()}"
    
    channel.basic_publish(
        exchange='transaction.exchange',
        routing_key=routing_key,
        body=json.dumps(event),
        properties=pika.BasicProperties(
            content_type='application/json',
            delivery_mode=2  # Persistent
        )
    )
    
    print(f"✅ Événement publié avec succès!")
    print(f"   Transaction ID: {transaction_id}")
    print(f"   Type: {event_type}")
    print(f"   Routing Key: {routing_key}")
    print(f"   Montant: {event['amount']} {event['currency']}")
    
    connection.close()

def main():
    """Fonction principale"""
    
    print("=" * 60)
    print("Test RabbitMQ - Microservice Notifications")
    print("=" * 60)
    
    if len(sys.argv) > 1:
        event_type = sys.argv[1].upper()
    else:
        print("\nTypes d'événements disponibles:")
        print("1. DEPOSIT")
        print("2. WITHDRAWAL")
        print("3. TRANSFER")
        print("4. PAYMENT")
        
        choice = input("\nChoisissez un type (1-4) ou appuyez sur Entrée pour DEPOSIT: ").strip()
        
        types = {
            '1': 'DEPOSIT',
            '2': 'WITHDRAWAL',
            '3': 'TRANSFER',
            '4': 'PAYMENT'
        }
        
        event_type = types.get(choice, 'DEPOSIT')
    
    try:
        publish_transaction_event(event_type)
        print("\n✅ Test terminé avec succès!")
        print("\nVérifiez:")
        print("- Les logs du microservice")
        print("- L'interface RabbitMQ: http://localhost:15672")
        print("- Votre boîte email")
        
    except Exception as e:
        print(f"\n❌ Erreur: {e}")
        print("\nAssurez-vous que:")
        print("- RabbitMQ est démarré (rabbitmq-server)")
        print("- Le microservice est démarré")
        sys.exit(1)

if __name__ == "__main__":
    main()
