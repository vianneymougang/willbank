# Script PowerShell pour créer des notifications de test

Write-Host "=========================================="
Write-Host "Création de Notifications de Test"
Write-Host "=========================================="
Write-Host ""

$apiUrl = "http://localhost:8082/api/notifications/history/send"

# Vérifier que l'API est accessible
try {
    $stats = Invoke-RestMethod -Uri "http://localhost:8082/api/notifications/history/stats" -Method Get
    Write-Host "✅ Microservice accessible" -ForegroundColor Green
    Write-Host "   Total actuel: $($stats.total)" -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host "❌ Erreur: Le microservice n'est pas accessible" -ForegroundColor Red
    Write-Host "   Démarrez-le avec: docker-compose up -d" -ForegroundColor Yellow
    exit 1
}

# Créer des notifications de test
$notifications = @(
    @{
        recipient = "admin@willbank.com"
        subject = "Client créé - Jean Dupont"
        content = "<html><body><h2>Client créé</h2><p>Un nouveau client a été créé avec succès.</p><table border='1' cellpadding='10'><tr><td><strong>Nom:</strong></td><td>Jean Dupont</td></tr><tr><td><strong>Email:</strong></td><td>jean.dupont@example.com</td></tr><tr><td><strong>Téléphone:</strong></td><td>+33612345678</td></tr></table></body></html>"
        type = "EMAIL"
        userId = "admin"
    },
    @{
        recipient = "admin@willbank.com"
        subject = "Transaction effectuée - 1000 EUR"
        content = "<html><body><h2>Transaction effectuée</h2><p>Une transaction a été effectuée avec succès.</p><table border='1' cellpadding='10'><tr><td><strong>Montant:</strong></td><td>1000 EUR</td></tr><tr><td><strong>Type:</strong></td><td>DEPOSIT</td></tr><tr><td><strong>Statut:</strong></td><td>COMPLETED</td></tr></table></body></html>"
        type = "EMAIL"
        userId = "admin"
        transactionId = "TXN-2024-001"
    },
    @{
        recipient = "admin@willbank.com"
        subject = "Compte créé - FR7612345678901234567890123"
        content = "<html><body><h2>Compte créé</h2><p>Un nouveau compte bancaire a été créé.</p><table border='1' cellpadding='10'><tr><td><strong>Numéro:</strong></td><td>FR7612345678901234567890123</td></tr><tr><td><strong>Type:</strong></td><td>Compte Courant</td></tr><tr><td><strong>Solde:</strong></td><td>0 EUR</td></tr></table></body></html>"
        type = "EMAIL"
        userId = "admin"
    },
    @{
        recipient = "manager@willbank.com"
        subject = "Client modifié - Marie Martin"
        content = "<html><body><h2>Client modifié</h2><p>Les informations d'un client ont été mises à jour.</p><table border='1' cellpadding='10'><tr><td><strong>Nom:</strong></td><td>Marie Martin</td></tr><tr><td><strong>Email:</strong></td><td>marie.martin@example.com</td></tr></table></body></html>"
        type = "EMAIL"
        userId = "admin"
    },
    @{
        recipient = "admin@willbank.com"
        subject = "Transaction effectuée - 500 EUR"
        content = "<html><body><h2>Transaction effectuée</h2><p>Retrait effectué avec succès.</p><table border='1' cellpadding='10'><tr><td><strong>Montant:</strong></td><td>500 EUR</td></tr><tr><td><strong>Type:</strong></td><td>WITHDRAWAL</td></tr><tr><td><strong>Statut:</strong></td><td>COMPLETED</td></tr></table></body></html>"
        type = "EMAIL"
        userId = "user123"
        transactionId = "TXN-2024-002"
    }
)

$successCount = 0
$errorCount = 0

foreach ($notification in $notifications) {
    try {
        $body = $notification | ConvertTo-Json
        $response = Invoke-RestMethod -Uri $apiUrl -Method Post -ContentType "application/json" -Body $body
        Write-Host "✅ Notification créée: $($notification.subject)" -ForegroundColor Green
        $successCount++
        Start-Sleep -Milliseconds 500
    } catch {
        Write-Host "❌ Erreur: $($notification.subject)" -ForegroundColor Red
        Write-Host "   $($_.Exception.Message)" -ForegroundColor Yellow
        $errorCount++
    }
}

Write-Host ""
Write-Host "=========================================="
Write-Host "Résumé"
Write-Host "=========================================="
Write-Host "✅ Créées: $successCount" -ForegroundColor Green
Write-Host "❌ Erreurs: $errorCount" -ForegroundColor Red
Write-Host ""

# Afficher les nouvelles statistiques
try {
    $newStats = Invoke-RestMethod -Uri "http://localhost:8082/api/notifications/history/stats" -Method Get
    Write-Host "📊 Statistiques actuelles:" -ForegroundColor Cyan
    Write-Host "   Total: $($newStats.total)" -ForegroundColor White
    Write-Host "   Envoyées: $($newStats.sent)" -ForegroundColor Green
    Write-Host "   En attente: $($newStats.pending)" -ForegroundColor Yellow
    Write-Host "   Échouées: $($newStats.failed)" -ForegroundColor Red
} catch {
    Write-Host "⚠️  Impossible de récupérer les statistiques" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎉 Terminé!" -ForegroundColor Green
Write-Host ""
Write-Host "Accédez à http://localhost:4200/notifications pour voir les notifications" -ForegroundColor Cyan
Write-Host ""
