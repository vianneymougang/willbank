import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PrimengModule } from '../primeng.module'; // Shared PrimeNG imports
import { ReusableFormComponent } from './reusable-form/reusable-form.component';
import { ReusableTableComponent } from './reusable-table/reusable-table.component';

@NgModule({
  declarations: [
    ReusableFormComponent,
    ReusableTableComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    PrimengModule
  ],
  exports: [
    ReusableFormComponent,
    ReusableTableComponent,
    FormsModule,
    ReactiveFormsModule,
    PrimengModule
  ]
})
export class SharedModule { }