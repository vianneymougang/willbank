import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccountsRoutingModule } from './accounts-routing.module';
import { PrimengModule } from '../../primeng.module';
import { SharedModule } from '../../shared/shared.module';
import { AccountListComponent } from './account-list/account-list.component';
import { AccountDetailComponent } from './account-detail/account-detail.component';
import { AccountFormComponent } from './account-form/account-form.component';

@NgModule({
  declarations: [
    AccountListComponent,
    AccountDetailComponent,
    AccountFormComponent
  ],
  imports: [
    CommonModule,
    AccountsRoutingModule,
    PrimengModule,
    SharedModule
  ]
})
export class AccountsModule { }