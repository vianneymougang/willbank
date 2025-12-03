import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-dashboard-client',
  templateUrl: './dashboard-client.component.html',
  styleUrls: ['./dashboard-client.component.scss']
})
export class DashboardClientComponent implements OnInit {
  dashboardData: any;
  clientId = 1; // Placeholder, à rendre dynamique (e.g., via route ou sélection)

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.apiService.getClientDashboard(this.clientId).subscribe(data => this.dashboardData = data);
  }
}