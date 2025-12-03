import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-client-dashboard',
  templateUrl: './client-dashboard.component.html',
  styleUrls: ['./client-dashboard.component.scss']
})
export class ClientDashboardComponent implements OnInit {
  dashboardData: any;
  clientId = 1; // Exemple, à rendre dynamique (e.g., from route or user)

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getClientDashboard(this.clientId).subscribe(data => this.dashboardData = data);
  }
}