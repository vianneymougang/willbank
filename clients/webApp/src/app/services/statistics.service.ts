import { Injectable } from '@angular/core';
import { Observable, forkJoin } from 'rxjs';
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
          clientsGrowth: 12.5,
          accountsGrowth: 8.3,
          transactionsGrowth: 15.7,
          balanceGrowth: 10.2
        };
      })
    );
  }

  getClientsChartData(): Observable<ChartData> {
    return this.apiService.getClients().pipe(
      map(clients => {
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
