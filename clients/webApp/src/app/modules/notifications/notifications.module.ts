import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationsRoutingModule } from './notifications-routing.module';
import { PrimengModule } from '../../primeng.module';
import { SharedModule } from '../../shared/shared.module';
import { NotificationTestComponent } from './notification-test/notification-test.component';

@NgModule({
  declarations: [
    NotificationTestComponent
  ],
  imports: [
    CommonModule,
    NotificationsRoutingModule,
    PrimengModule,
    SharedModule
  ]
})
export class NotificationsModule { }