import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-client-list',
  templateUrl: './client-list.component.html',
  styleUrls: ['./client-list.component.scss']
})
export class ClientListComponent implements OnInit {
  clients: any[] = [];

  constructor(private apiService: ApiService, private router: Router) {}

  ngOnInit() {
    this.apiService.getClients().subscribe(data => this.clients = data);
  }

  view(id: number) {
    this.router.navigate(['/clients', id]);
  }

  edit(id: number) {
    this.router.navigate(['/clients', id, 'edit']);
  }

  delete(id: number) {
    this.apiService.deleteClient(id).subscribe(() => this.clients = this.clients.filter(c => c.id !== id));
  }

  add() {
    this.router.navigate(['/clients/new']);
  }
}