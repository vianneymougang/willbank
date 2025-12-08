# 📊 Guide d'Implémentation des Graphiques Dashboard

## 🎯 Objectif
Ajouter des graphiques interactifs au dashboard admin pour visualiser les statistiques bancaires.

---

## 📦 Étape 1 : Installation des dépendances

```bash
npm install chart.js ng2-charts
```

---

## 🔧 Étape 2 : Créer le service de statistiques

### Fichier : `src/app/services/statistics.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, delay } from 'rxjs';
import { environment } from '../../environments/environment';

const MOCK_MODE = true;

export interface DashboardStats {
  totalClients: number;
  totalComptes: number;
  totalTransactions: number;
  soldeTotal: number;
  evolutionClients: number[];
  evolutionTransactions: number[];
  repartitionComptes: { type: string; count: number }[];
  transactionsParMois: { mois: string; montant: number }[];
}

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {
  private baseUrl = `${environment.apiGatewayUrl}/api/v1/statistics`;

  constructor(private http: HttpClient) {}

  getDashboardStats(): Observable<DashboardStats> {
    if (MOCK_MODE) {
      const mockStats: DashboardStats = {
        totalClients: 1234,
        totalComptes: 2456,
        totalTransactions: 8945,
        soldeTotal: 4520000,
        evolutionClients: [100, 150, 200, 280, 350, 420, 500, 580, 650, 720, 850, 1000],
        evolutionTransactions: [500, 650, 800, 950, 1100, 1250, 1400, 1550, 1700, 1850, 2000, 2150],
        repartitionComptes: [
          { type: 'Courant', count: 1200 },
          { type: 'Épargne', count: 856 },
          { type: 'Professionnel', count: 400 }
        ],
        transactionsParMois: [
          { mois: 'Jan', montant: 125000 },
          { mois: 'Fév', montant: 145000 },
          { mois: 'Mar', montant: 165000 },
          { mois: 'Avr', montant: 155000 },
          { mois: 'Mai', montant: 185000 },
          { mois: 'Jun', montant: 195000 }
        ]
      };
      return of(mockStats).pipe(delay(500));
    }
    
    return this.http.get<DashboardStats>(`${this.baseUrl}/dashboard`);
  }
}
```

---

## 📝 Étape 3 : Mettre à jour le Dashboard Component

### Fichier : `src/app/pages/dashboard/dashboard.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';
import { StatisticsService, DashboardStats } from '../../services/statistics.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, CardModule, ChartModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  stats?: DashboardStats;
  
  // Configuration des graphiques
  lineChartData: any;
  lineChartOptions: any;
  
  pieChartData: any;
  pieChartOptions: any;
  
  barChartData: any;
  barChartOptions: any;

  constructor(private statisticsService: StatisticsService) {}

  ngOnInit() {
    this.loadStatistics();
  }

  loadStatistics() {
    this.statisticsService.getDashboardStats().subscribe(stats => {
      this.stats = stats;
      this.initializeCharts();
    });
  }

  initializeCharts() {
    if (!this.stats) return;

    // Graphique en ligne - Évolution des clients
    this.lineChartData = {
      labels: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc'],
      datasets: [
        {
          label: 'Clients',
          data: this.stats.evolutionClients,
          fill: false,
          borderColor: '#667eea',
          tension: 0.4
        },
        {
          label: 'Transactions',
          data: this.stats.evolutionTransactions,
          fill: false,
          borderColor: '#f5576c',
          tension: 0.4
        }
      ]
    };

    this.lineChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top'
        },
        title: {
          display: true,
          text: 'Évolution annuelle'
        }
      }
    };

    // Graphique circulaire - Répartition des comptes
    this.pieChartData = {
      labels: this.stats.repartitionComptes.map(r => r.type),
      datasets: [
        {
          data: this.stats.repartitionComptes.map(r => r.count),
          backgroundColor: ['#667eea', '#f5576c', '#4facfe'],
          hoverBackgroundColor: ['#5568d3', '#e04658', '#3a9ae7']
        }
      ]
    };

    this.pieChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'bottom'
        },
        title: {
          display: true,
          text: 'Répartition des comptes'
        }
      }
    };

    // Graphique en barres - Transactions par mois
    this.barChartData = {
      labels: this.stats.transactionsParMois.map(t => t.mois),
      datasets: [
        {
          label: 'Montant (€)',
          data: this.stats.transactionsParMois.map(t => t.montant),
          backgroundColor: '#43e97b',
          borderColor: '#38f9d7',
          borderWidth: 1
        }
      ]
    };

    this.barChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        },
        title: {
          display: true,
          text: 'Volume de transactions'
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: function(value: any) {
              return value.toLocaleString() + ' €';
            }
          }
        }
      }
    };
  }
}
```

---

## 🎨 Étape 4 : Mettre à jour le template HTML

### Ajouter dans `src/app/pages/dashboard/dashboard.component.html`

Après la section `.stats-grid`, ajouter :

```html
<!-- Section Graphiques -->
<div class="charts-section" *ngIf="stats">
  <div class="charts-grid">
    <!-- Graphique Évolution -->
    <div class="chart-card">
      <p-card>
        <ng-template pTemplate="header">
          <div class="card-header-custom">
            <i class="pi pi-chart-line"></i>
            <span>Évolution annuelle</span>
          </div>
        </ng-template>
        <div class="chart-container">
          <p-chart type="line" [data]="lineChartData" [options]="lineChartOptions"></p-chart>
        </div>
      </p-card>
    </div>

    <!-- Graphique Répartition -->
    <div class="chart-card">
      <p-card>
        <ng-template pTemplate="header">
          <div class="card-header-custom">
            <i class="pi pi-chart-pie"></i>
            <span>Répartition des comptes</span>
          </div>
        </ng-template>
        <div class="chart-container">
          <p-chart type="pie" [data]="pieChartData" [options]="pieChartOptions"></p-chart>
        </div>
      </p-card>
    </div>

    <!-- Graphique Transactions -->
    <div class="chart-card full-width">
      <p-card>
        <ng-template pTemplate="header">
          <div class="card-header-custom">
            <i class="pi pi-chart-bar"></i>
            <span>Volume de transactions</span>
          </div>
        </ng-template>
        <div class="chart-container">
          <p-chart type="bar" [data]="barChartData" [options]="barChartOptions"></p-chart>
        </div>
      </p-card>
    </div>
  </div>
</div>
```

---

## 🎨 Étape 5 : Ajouter les styles CSS

### Ajouter dans `src/app/pages/dashboard/dashboard.component.scss`

```scss
.charts-section {
  margin-top: 2rem;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-bottom: 2rem;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  &.full-width {
    grid-column: 1 / -1;
  }
}

.chart-container {
  height: 300px;
  padding: 1rem;
}

.card-header-custom {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;

  i {
    font-size: 1.2rem;
  }
}
```

---

## 🔄 Étape 6 : Mettre à jour les stats dynamiques

### Modifier le template pour utiliser les vraies données

Remplacer les valeurs hardcodées dans `.stats-grid` :

```html
<div class="stats-grid" *ngIf="stats">
  <div class="stat-card">
    <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
      <i class="pi pi-users"></i>
    </div>
    <div class="stat-content">
      <h3>Clients</h3>
      <p class="stat-number">{{ stats.totalClients | number }}</p>
      <span class="stat-change positive"><i class="pi pi-arrow-up"></i> +12%</span>
    </div>
  </div>

  <div class="stat-card">
    <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
      <i class="pi pi-wallet"></i>
    </div>
    <div class="stat-content">
      <h3>Comptes</h3>
      <p class="stat-number">{{ stats.totalComptes | number }}</p>
      <span class="stat-change positive"><i class="pi pi-arrow-up"></i> +8%</span>
    </div>
  </div>

  <div class="stat-card">
    <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
      <i class="pi pi-money-bill"></i>
    </div>
    <div class="stat-content">
      <h3>Transactions</h3>
      <p class="stat-number">{{ stats.totalTransactions | number }}</p>
      <span class="stat-change positive"><i class="pi pi-arrow-up"></i> +15%</span>
    </div>
  </div>

  <div class="stat-card">
    <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
      <i class="pi pi-dollar"></i>
    </div>
    <div class="stat-content">
      <h3>Solde Total</h3>
      <p class="stat-number">{{ stats.soldeTotal | currency:'EUR':'symbol':'1.0-0' }}</p>
      <span class="stat-change positive"><i class="pi pi-arrow-up"></i> +5%</span>
    </div>
  </div>
</div>
```

---

## ✅ Résultat Final

Après ces modifications, votre dashboard aura :

1. ✅ **Statistiques dynamiques** connectées au backend
2. ✅ **Graphique en ligne** pour l'évolution des clients et transactions
3. ✅ **Graphique circulaire** pour la répartition des comptes
4. ✅ **Graphique en barres** pour le volume de transactions
5. ✅ **Design responsive** et moderne
6. ✅ **Données mockées** pour le développement sans backend

---

## 🔌 Connexion au Backend Java

Quand le backend sera prêt, il suffit de :

1. Mettre `MOCK_MODE = false` dans `statistics.service.ts`
2. Créer l'endpoint dans votre API Gateway : `GET /api/v1/statistics/dashboard`
3. Le backend doit retourner un JSON au format `DashboardStats`

### Exemple de réponse attendue du backend :

```json
{
  "totalClients": 1234,
  "totalComptes": 2456,
  "totalTransactions": 8945,
  "soldeTotal": 4520000,
  "evolutionClients": [100, 150, 200, 280, 350, 420, 500, 580, 650, 720, 850, 1000],
  "evolutionTransactions": [500, 650, 800, 950, 1100, 1250, 1400, 1550, 1700, 1850, 2000, 2150],
  "repartitionComptes": [
    { "type": "Courant", "count": 1200 },
    { "type": "Épargne", "count": 856 },
    { "type": "Professionnel", "count": 400 }
  ],
  "transactionsParMois": [
    { "mois": "Jan", "montant": 125000 },
    { "mois": "Fév", "montant": 145000 },
    { "mois": "Mar", "montant": 165000 },
    { "mois": "Avr", "montant": 155000 },
    { "mois": "Mai", "montant": 185000 },
    { "mois": "Jun", "montant": 195000 }
  ]
}
```

---

## 🎯 Prochaines étapes

- [ ] Installer les dépendances Chart.js
- [ ] Créer le service de statistiques
- [ ] Mettre à jour le dashboard component
- [ ] Tester avec les données mockées
- [ ] Connecter au backend Java quand prêt
