import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.scss']
})
export class TransactionListComponent implements OnInit {
  transactions: any[] = [];

  constructor(
    private apiService: ApiService, 
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.apiService.getTransactions().subscribe(data => this.transactions = data);
  }

  view(id: number) {
    this.router.navigate(['/transactions', id]);
  }

  edit(id: number) {
    this.router.navigate(['/transactions', id, 'edit']);
  }

  delete(id: number) {
    const transaction = this.transactions.find(t => t.id === id);
    if (confirm('Êtes-vous sûr de vouloir supprimer cette transaction ?')) {
      this.apiService.deleteTransaction(id).subscribe(() => {
        this.transactions = this.transactions.filter(t => t.id !== id);
        
        // Notification de suppression
        this.notificationService.addNotification({
          type: 'error',
          title: 'Transaction supprimée',
          message: `La transaction de ${transaction?.montant}€ a été supprimée`,
          read: false
        });
      });
    }
  }

  add() {
    this.router.navigate(['/transactions/new']);
  }
}