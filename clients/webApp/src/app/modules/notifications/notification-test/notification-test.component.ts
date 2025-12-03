import { Component } from '@angular/core';
import { ApiService } from '../../../services/api.service';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-notification-test',
  templateUrl: './notification-test.component.html',
  styleUrls: ['./notification-test.component.scss']
})
export class NotificationTestComponent {
  notification = { type: '', message: '', recipient: '' };

  constructor(private apiService: ApiService, private messageService: MessageService) {}

  send() {
    this.apiService.sendNotification(this.notification).subscribe(() => {
      this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Notification envoyée' });
    });
  }
}