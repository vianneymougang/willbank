import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-statement',
  templateUrl: './statement.component.html',
  styleUrls: ['./statement.component.scss']
})
export class StatementComponent implements OnInit {
  statementData: any[] = [];
  accountId?: number;
  period = 'last_month'; // Exemple, à rendre configurable

  constructor(private route: ActivatedRoute, private apiService: ApiService) {}

  ngOnInit() {
    this.accountId = +this.route.snapshot.paramMap.get('accountId')!;
    if (this.accountId) {
      this.apiService.getAccountStatement(this.accountId, this.period).subscribe(data => this.statementData = data);
    }
  }
}