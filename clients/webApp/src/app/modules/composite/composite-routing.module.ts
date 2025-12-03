import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardClientComponent } from './dashboard-client/dashboard-client.component';

const routes: Routes = [
  { path: 'dashboard-client', component: DashboardClientComponent }
  // Ajoutez plus pour relevé, recherche transactions
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CompositeRoutingModule { }