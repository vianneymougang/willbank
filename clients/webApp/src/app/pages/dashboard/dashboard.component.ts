import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ChartModule } from 'primeng/chart';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { StatisticsService, DashboardStats } from '../../services/statistics.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, CardModule, ButtonModule, TagModule, RouterLink, ChartModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  recentClients: any[] = [];
  recentTransactions: any[] = [];
  recentAccounts: any[] = [];
  recentNotifications: any[] = [];
  
  stats: DashboardStats | null = null;
  clientsChartData: any;
  accountsChartData: any;
  transactionsChartData: any;
  
  loading = true;

  chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const
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
    private statisticsService: StatisticsService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
    this.loadStatistics();
    this.loadCharts();
  }

  loadDashboardData(): void {
    this.loading = true;

    // Charger les 5 derniers clients
    this.apiService.getClients().subscribe({
      next: (clients) => {
        this.recentClients = clients.slice(-5).reverse();
      },
      error: (error) => console.error('Erreur chargement clients:', error)
    });

    // Charger les 5 dernières transactions
    this.apiService.getTransactions().subscribe({
      next: (transactions) => {
        this.recentTransactions = transactions.slice(-5).reverse();
      },
      error: (error) => console.error('Erreur chargement transactions:', error)
    });

    // Charger les 3 derniers comptes
    this.apiService.getAccounts().subscribe({
      next: (accounts) => {
        this.recentAccounts = accounts.slice(-3).reverse();
      },
      error: (error) => console.error('Erreur chargement comptes:', error)
    });

    // Charger les 3 dernières notifications
    this.notificationService.getNotifications().subscribe({
      next: (notifications) => {
        this.recentNotifications = notifications.slice(0, 3);
        this.loading = false;
      },
      error: (error) => {
        console.error('Erreur chargement notifications:', error);
        this.loading = false;
      }
    });
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

  getTransactionTypeLabel(type: string): string {
    const labels: any = {
      'virement': 'Virement',
      'depot': 'Dépôt',
      'retrait': 'Retrait'
    };
    return labels[type] || type;
  }

  getTransactionTypeSeverity(type: string): 'success' | 'info' | 'warning' | 'danger' | 'secondary' | 'contrast' {
    const severities: Record<string, 'success' | 'info' | 'warning' | 'danger' | 'secondary' | 'contrast'> = {
      'virement': 'info',
      'depot': 'success',
      'retrait': 'warning'
    };
    return severities[type] || 'info';
  }

  getAccountTypeLabel(type: string): string {
    const labels: any = {
      'courant': 'Courant',
      'epargne': 'Épargne',
      'professionnel': 'Professionnel'
    };
    return labels[type] || type;
  }

  getNotificationIcon(type: string): string {
    const icons: any = {
      'success': 'pi-check-circle',
      'info': 'pi-info-circle',
      'warning': 'pi-exclamation-triangle',
      'error': 'pi-times-circle'
    };
    return icons[type] || 'pi-bell';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 60) {
      return `Il y a ${diffMins} min`;
    } else if (diffHours < 24) {
      return `Il y a ${diffHours}h`;
    } else if (diffDays < 7) {
      return `Il y a ${diffDays}j`;
    } else {
      return date.toLocaleDateString('fr-FR');
    }
  }
}
