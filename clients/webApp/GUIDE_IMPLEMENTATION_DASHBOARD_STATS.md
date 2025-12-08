# 📊 Guide d'Implémentation - Dashboard avec Statistiques et Graphiques

**Date:** 8 Décembre 2025  
**Objectif:** Ajouter des statistiques globales et des graphiques au dashboard

---

## 🎯 Vue d'Ensemble

Le dashboard affichera :
1. **Cartes de statistiques** (4 cartes en haut)
2. **Graphiques** (2-3 graphiques)
3. **Derniers éléments** (blocs existants)

---

## 📦 Étape 1 : Installation de Chart.js

```bash
npm install chart.js ng2-charts
```

---

## 📊 Étape 2 : Créer le Service de Statistiques

**Fichier:** `src/app/services/statistics.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { Observable, of, delay, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';
import { ApiService } from './api.service';

export interface DashboardStats {
  totalClients: number;
  totalAccounts: number;
  totalTransactions: number;
  totalBalance: number;
  clientsGrowth: number;
  accountsGrowth: number;
  transactionsGrowth: number;
  balanceGrowth: number;
}

export interface ChartData {
  labels: string[];
  datasets: any[];
}

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {
  
  constructor(private apiService: ApiService) {}

  /**
   * Récupère les statistiques globales du dashboard
   */
  getDashboardStats(): Observable<DashboardStats> {
    return forkJoin({
      clients: this.apiService.getClients(),
      accounts: this.apiService.getAccounts(),
      transactions: this.apiService.getTransactions()
    }).pipe(
      map(data => {
        const totalBalance = data.accounts.reduce((sum, acc) => sum + acc.solde, 0);
        
        return {
          totalClients: data.clients.length,
          totalAccounts: data.accounts.length,
          totalTransactions: data.transactions.length,
          totalBalance: totalBalance,
          clientsGrowth: 12.5,  // Pourcentage de croissance (à calculer avec données historiques)
          accountsGrowth: 8.3,
          transactionsGrowth: 15.7,
          balanceGrowth: 10.2
        };
      })
    );
  }

  /**
   * Données pour le graphique d'évolution des clients
   */
  getClientsChartData(): Observable<ChartData> {
    return this.apiService.getClients().pipe(
      map(clients => {
        // Grouper par mois (simulation)
        const months = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun'];
        const data = [10, 15, 22, 28, 35, clients.length];
        
        return {
          labels: months,
          datasets: [{
            label: 'Clients',
            data: data,
            borderColor: '#667eea',
            backgroundColor: 'rgba(102, 126, 234, 0.1)',
            tension: 0.4,
            fill: true
          }]
        };
      })
    );
  }

  /**
   * Données pour le graphique de répartition des comptes
   */
  getAccountsChartData(): Observable<ChartData> {
    return this.apiService.getAccounts().pipe(
      map(accounts => {
        const courant = accounts.filter(a => a.type === 'courant').length;
        const epargne = accounts.filter(a => a.type === 'epargne').length;
        const professionnel = accounts.filter(a => a.type === 'professionnel').length;
        
        return {
          labels: ['Courant', 'Épargne', 'Professionnel'],
          datasets: [{
            data: [courant, epargne, professionnel],
            backgroundColor: [
              '#4facfe',
              '#43e97b',
              '#f093fb'
            ],
            borderWidth: 0
          }]
        };
      })
    );
  }

  /**
   * Données pour le graphique des transactions par type
   */
  getTransactionsChartData(): Observable<ChartData> {
    return this.apiService.getTransactions().pipe(
      map(transactions => {
        const virements = transactions.filter(t => t.type === 'virement').length;
        const depots = transactions.filter(t => t.type === 'depot').length;
        const retraits = transactions.filter(t => t.type === 'retrait').length;
        
        return {
          labels: ['Virements', 'Dépôts', 'Retraits'],
          datasets: [{
            label: 'Transactions',
            data: [virements, depots, retraits],
            backgroundColor: [
              '#4facfe',
              '#43e97b',
              '#fa709a'
            ],
            borderWidth: 0
          }]
        };
      })
    );
  }
}
```

---

## 🎨 Étape 3 : Modifier le Dashboard Component

**Fichier:** `src/app/pages/dashboard/dashboard.component.ts`

Ajouter les imports et propriétés :

```typescript
import { ChartModule } from 'primeng/chart';
import { StatisticsService, DashboardStats } from '../../services/statistics.service';

// Dans la classe
stats: DashboardStats | null = null;
clientsChartData: any;
accountsChartData: any;
transactionsChartData: any;

chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'bottom'
    }
  }
};

lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    }
  },
  scales: {
    y: {
      beginAtZero: true
    }
  }
};

constructor(
  private apiService: ApiService,
  private notificationService: NotificationService,
  private statisticsService: StatisticsService  // Ajouter
) {}

ngOnInit(): void {
  this.loadDashboardData();
  this.loadStatistics();  // Ajouter
  this.loadCharts();      // Ajouter
}

loadStatistics(): void {
  this.statisticsService.getDashboardStats().subscribe({
    next: (stats) => {
      this.stats = stats;
    },
    error: (error) => console.error('Erreur chargement stats:', error)
  });
}

loadCharts(): void {
  this.statisticsService.getClientsChartData().subscribe({
    next: (data) => {
      this.clientsChartData = data;
    }
  });

  this.statisticsService.getAccountsChartData().subscribe({
    next: (data) => {
      this.accountsChartData = data;
    }
  });

  this.statisticsService.getTransactionsChartData().subscribe({
    next: (data) => {
      this.transactionsChartData = data;
    }
  });
}
```

Mettre à jour les imports du component :

```typescript
imports: [
  CommonModule, 
  CardModule, 
  ButtonModule, 
  TagModule, 
  RouterLink,
  ChartModule  // Ajouter
],
```

---

## 📄 Étape 4 : Modifier le Template HTML

**Fichier:** `src/app/pages/dashboard/dashboard.component.html`

Ajouter après le header, avant les sections :

```html
<!-- Statistiques Globales -->
<div class="stats-grid" *ngIf="stats">
  <div class="stat-card">
    <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
      <i class="pi pi-users"></i>
    </div>
    <div class="stat-content">
      <span class="stat-label">Total Clients</span>
      <h2 class="stat-value">{{ stats.totalClients }}</h2>
      <span class="stat-growth positive">
        <i class="pi pi-arrow-up"></i> {{ stats.clientsGrowth }}%
      </span>
    </div>
  </div>

  <div class="stat-card">
    <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
      <i class="pi pi-wallet"></i>
    </div>
    <div class="stat-content">
      <span class="stat-label">Total Comptes</span>
      <h2 class="stat-value">{{ stats.totalAccounts }}</h2>
      <span class="stat-growth positive">
        <i class="pi pi-arrow-up"></i> {{ stats.accountsGrowth }}%
      </span>
    </div>
  </div>

  <div class="stat-card">
    <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
      <i class="pi pi-money-bill"></i>
    </div>
    <div class="stat-content">
      <span class="stat-label">Transactions</span>
      <h2 class="stat-value">{{ stats.totalTransactions }}</h2>
      <span class="stat-growth positive">
        <i class="pi pi-arrow-up"></i> {{ stats.transactionsGrowth }}%
      </span>
    </div>
  </div>

  <div class="stat-card">
    <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
      <i class="pi pi-euro"></i>
    </div>
    <div class="stat-content">
      <span class="stat-label">Solde Total</span>
      <h2 class="stat-value">{{ stats.totalBalance | number:'1.0-0' }} €</h2>
      <span class="stat-growth positive">
        <i class="pi pi-arrow-up"></i> {{ stats.balanceGrowth }}%
      </span>
    </div>
  </div>
</div>

<!-- Graphiques -->
<div class="charts-grid">
  <div class="chart-card">
    <h3><i class="pi pi-chart-line"></i> Évolution des Clients</h3>
    <p-chart type="line" [data]="clientsChartData" [options]="lineChartOptions" height="300px"></p-chart>
  </div>

  <div class="chart-card">
    <h3><i class="pi pi-chart-pie"></i> Répartition des Comptes</h3>
    <p-chart type="doughnut" [data]="accountsChartData" [options]="chartOptions" height="300px"></p-chart>
  </div>

  <div class="chart-card">
    <h3><i class="pi pi-chart-bar"></i> Transactions par Type</h3>
    <p-chart type="bar" [data]="transactionsChartData" [options]="chartOptions" height="300px"></p-chart>
  </div>
</div>
```

---

## 🎨 Étape 5 : Ajouter les Styles

**Fichier:** `src/app/pages/dashboard/dashboard.component.scss`

Ajouter après les styles existants :

```scss
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 1.5rem;
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }
  
  .stat-icon {
    width: 70px;
    height: 70px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 2rem;
    flex-shrink: 0;
  }
  
  .stat-content {
    flex: 1;
    
    .stat-label {
      font-size: 0.875rem;
      color: #7f8c8d;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.5px;
    }
    
    .stat-value {
      font-size: 2rem;
      font-weight: 700;
      color: var(--text-dark);
      margin: 0.5rem 0;
    }
    
    .stat-growth {
      font-size: 0.875rem;
      font-weight: 600;
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
      
      &.positive {
        color: #00B894;
      }
      
      &.negative {
        color: #d63031;
      }
    }
  }
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  
  h3 {
    font-size: 1.1rem;
    font-weight: 600;
    color: var(--text-dark);
    margin: 0 0 1.5rem 0;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    
    i {
      color: var(--primary);
    }
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .stat-card {
    padding: 1.25rem;
    
    .stat-icon {
      width: 60px;
      height: 60px;
      font-size: 1.75rem;
    }
    
    .stat-value {
      font-size: 1.75rem;
    }
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
}
```

---

## ✅ Résultat Final

Le dashboard affichera :

1. **4 Cartes de Statistiques** en haut
   - Total Clients avec croissance
   - Total Comptes avec croissance
   - Total Transactions avec croissance
   - Solde Total avec croissance

2. **3 Graphiques**
   - Graphique en ligne : Évolution des clients
   - Graphique en donut : Répartition des comptes
   - Graphique en barres : Transactions par type

3. **Sections de Derniers Éléments** (existantes)
   - Derniers clients
   - Dernières transactions
   - Derniers comptes
   - Dernières notifications

---

## 🚀 Pour Tester

```bash
# 1. Installer Chart.js
npm install chart.js ng2-charts

# 2. Créer le service de statistiques
# Copier le code du service

# 3. Modifier le dashboard component
# Ajouter les imports et méthodes

# 4. Modifier le template
# Ajouter les cartes de stats et graphiques

# 5. Ajouter les styles
# Copier les styles SCSS

# 6. Lancer l'application
npm start
```

---

*Guide créé le 8 Décembre 2025*
