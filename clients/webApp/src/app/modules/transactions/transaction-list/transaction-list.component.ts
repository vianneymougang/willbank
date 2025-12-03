import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.scss']
})
export class TransactionListComponent implements OnInit {
  transactions: any[] = [];

  constructor(private apiService: ApiService, private router: Router) {}

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
    this.apiService.deleteTransaction(id).subscribe(() => this.transactions = this.transactions.filter(t => t.id !== id));
  }

  add() {
    this.router.navigate(['/transactions/new']);
  }
}