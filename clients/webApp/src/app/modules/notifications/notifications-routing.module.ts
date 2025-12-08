import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotificationListComponent } from './notification-list/notification-list.component';
import { NotificationSendComponent } from './notification-send/notification-send.component';
import { NotificationTestComponent } from './notification-test/notification-test.component';

const routes: Routes = [
  { path: '', component: NotificationListComponent },
  { path: 'send', component: NotificationSendComponent },
  { path: 'test', component: NotificationTestComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NotificationsRoutingModule { }