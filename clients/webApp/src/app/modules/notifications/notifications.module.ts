import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationsRoutingModule } from './notifications-routing.module';
import { PrimengModule } from '../../primeng.module';
import { SharedModule } from '../../shared/shared.module';
import { NotificationListComponent } from './notification-list/notification-list.component';
import { NotificationSendComponent } from './notification-send/notification-send.component';
import { NotificationTestComponent } from './notification-test/notification-test.component';

@NgModule({
  declarations: [
    NotificationListComponent,
    NotificationSendComponent,
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