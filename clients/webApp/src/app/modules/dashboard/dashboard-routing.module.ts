import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ClientDashboardComponent } from './client-dashboard/client-dashboard.component';
import { StatementComponent } from './statement/statement.component';
import { TransactionSearchComponent } from './transaction-search/transaction-search.component';

const routes: Routes = [
  { path: '', component: ClientDashboardComponent },
  { path: 'statement/:accountId', component: StatementComponent },
  { path: 'search', component: TransactionSearchComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DashboardRoutingModule { }