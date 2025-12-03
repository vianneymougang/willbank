import { Component } from '@angular/core';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-transaction-search',
  templateUrl: './transaction-search.component.html',
  styleUrls: ['./transaction-search.component.scss']
})
export class TransactionSearchComponent {
  searchParams = { type: '' }; // Ajoutez plus de params
  results: any[] = [];

  constructor(private apiService: ApiService) {}

  search() {
    this.apiService.searchTransactions(this.searchParams).subscribe(data => this.results = data);
  }
}