import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-account-list',
  templateUrl: './account-list.component.html',
  styleUrls: ['./account-list.component.scss']
})
export class AccountListComponent implements OnInit {
  accounts: any[] = [];

  constructor(private apiService: ApiService, private router: Router) {}

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
    this.apiService.deleteAccount(id).subscribe(() => this.accounts = this.accounts.filter(a => a.id !== id));
  }

  add() {
    this.router.navigate(['/accounts/new']);
  }
}