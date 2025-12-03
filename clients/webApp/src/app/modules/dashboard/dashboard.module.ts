import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardRoutingModule } from './dashboard-routing.module';
import { PrimengModule } from '../../primeng.module';
import { SharedModule } from '../../shared/shared.module';
import { ClientDashboardComponent } from './client-dashboard/client-dashboard.component';
import { StatementComponent } from './statement/statement.component';
import { TransactionSearchComponent } from './transaction-search/transaction-search.component';

@NgModule({
  declarations: [
    ClientDashboardComponent,
    StatementComponent,
    TransactionSearchComponent
  ],
  imports: [
    CommonModule,
    DashboardRoutingModule,
    PrimengModule,
    SharedModule
  ]
})
export class DashboardModule { }