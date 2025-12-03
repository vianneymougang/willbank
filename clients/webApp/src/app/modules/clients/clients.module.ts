import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClientsRoutingModule } from './clients-routing.module';
import { PrimengModule } from '../../primeng.module'; // Import shared PrimeNG
import { SharedModule } from '../../shared/shared.module';
import { ClientListComponent } from './client-list/client-list.component';
import { ClientDetailComponent } from './client-detail/client-detail.component';
import { ClientFormComponent } from './client-form/client-form.component';

@NgModule({
  declarations: [
    ClientListComponent,
    ClientDetailComponent,
    ClientFormComponent
  ],
  imports: [
    CommonModule,
    ClientsRoutingModule,
    PrimengModule,
    SharedModule
  ]
})
export class ClientsModule { }