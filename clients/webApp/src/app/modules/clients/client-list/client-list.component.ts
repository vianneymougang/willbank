import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { NotificationService } from '../../../services/notification.service';
import { Table } from 'primeng/table';

@Component({
  selector: 'app-client-list',
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.scss']
})
export class ClientListComponent implements OnInit {
  clients: any[] = [];
  searchText: string = '';

  constructor(
    private apiService: ApiService, 
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.loadClients();
  }

  loadClients() {
    this.apiService.getClients().subscribe(data => this.clients = data);
  }

  onSearch(event: Event, dt: Table) {
    const inputElement = event.target as HTMLInputElement;
    dt.filterGlobal(inputElement.value, 'contains');
  }

  searchClients() {
    if (this.searchText.trim() === '') {
      this.loadClients();
    } else {
      this.apiService.searchClients(this.searchText).subscribe(data => this.clients = data);
    }
  }

  view(id: number) {
    this.router.navigate(['/clients', id]);
  }

  edit(id: number) {
    this.router.navigate(['/clients', id, 'edit']);
  }

  delete(id: number) {
    const client = this.clients.find(c => c.id === id);
    if (confirm('Êtes-vous sûr de vouloir supprimer ce client ?')) {
      this.apiService.deleteClient(id).subscribe(() => {
        this.clients = this.clients.filter(c => c.id !== id);
        
        // Notification de suppression
        this.notificationService.addNotification({
          type: 'warning',
          title: 'Client supprimé',
          message: `Le client "${client?.nom} ${client?.prenom}" a été supprimé`,
          read: false
        });
      });
    }
  }

  add() {
    this.router.navigate(['/clients/new']);
  }
}