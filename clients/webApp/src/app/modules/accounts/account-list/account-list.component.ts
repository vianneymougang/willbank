import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-account-list',
  templateUrl: './account-list.component.html',
  styleUrls: ['./account-list.component.scss']
})
export class AccountListComponent implements OnInit {
  accounts: any[] = [];

  constructor(
    private apiService: ApiService, 
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.apiService.getAccounts().subscribe(data => this.accounts = data);
  }

  view(id: number) {
    this.router.navigate(['/accounts', id]);
  }

  edit(id: number) {
    this.router.navigate(['/accounts', id, 'edit']);
  }

  delete(id: number) {
    const account = this.accounts.find(a => a.id === id);
    if (confirm('Êtes-vous sûr de vouloir supprimer ce compte ?')) {
      this.apiService.deleteAccount(id).subscribe(() => {
        this.accounts = this.accounts.filter(a => a.id !== id);
        
        // Notification de suppression
        this.notificationService.addNotification({
          type: 'warning',
          title: 'Compte supprimé',
          message: `Le compte ${account?.type} (${account?.numeroCompte}) a été supprimé`,
          read: false
        });
      });
    }
  }

  add() {
    this.router.navigate(['/accounts/new']);
  }
}