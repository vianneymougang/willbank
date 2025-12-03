import { NgModule } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { PanelMenuModule } from 'primeng/panelmenu';
import { SidebarModule } from 'primeng/sidebar';
import { TableModule } from 'primeng/table';
import { ToolbarModule } from 'primeng/toolbar';
import { TooltipModule } from 'primeng/tooltip';
import { AvatarModule } from 'primeng/avatar';
import { BadgeModule } from 'primeng/badge';
import { RippleModule } from 'primeng/ripple';
import { DropdownModule } from 'primeng/dropdown';
import { PasswordModule } from 'primeng/password';

@NgModule({
  exports: [
    ButtonModule,
    CardModule,
    InputTextModule,
    InputTextareaModule,
    PanelMenuModule,
    SidebarModule,
    TableModule,
    ToolbarModule,
    TooltipModule,
    AvatarModule,
    BadgeModule,
    RippleModule,
    DropdownModule,
    PasswordModule
  ]
})
export class PrimengModule { }