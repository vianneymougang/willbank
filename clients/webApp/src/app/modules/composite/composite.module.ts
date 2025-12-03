import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CompositeRoutingModule } from './composite-routing.module';
import { PrimengModule } from '../../primeng.module';
import { SharedModule } from '../../shared/shared.module';
import { DashboardClientComponent } from './dashboard-client/dashboard-client.component';

@NgModule({
  declarations: [
    DashboardClientComponent
  ],
  imports: [
    CommonModule,
    CompositeRoutingModule,
    PrimengModule,
    SharedModule
  ]
})
export class CompositeModule { }