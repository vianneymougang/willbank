import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AccountListComponent } from './account-list/account-list.component';
import { AccountDetailComponent } from './account-detail/account-detail.component';
import { AccountFormComponent } from './account-form/account-form.component';

const routes: Routes = [
  { path: '', component: AccountListComponent },
  { path: 'new', component: AccountFormComponent },
  { path: ':id', component: AccountDetailComponent },
  { path: ':id/edit', component: AccountFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AccountsRoutingModule { }