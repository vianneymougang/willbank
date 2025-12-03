import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { NotificationTestComponent } from './notification-test/notification-test.component';

const routes: Routes = [
  { path: '', component: NotificationTestComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class NotificationsRoutingModule { }