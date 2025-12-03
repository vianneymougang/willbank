import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-notification-send',
  templateUrl: './notification-send.component.html',
  styleUrls: ['./notification-send.component.scss']
})
export class NotificationSendComponent {
  notificationForm: FormGroup;

  constructor(private fb: FormBuilder, private router: Router, private apiService: ApiService) {
    this.notificationForm = this.fb.group({
      type: ['', Validators.required],
      destinataire: ['', Validators.required],
      message: ['', Validators.required]
    });
  }

  save() {
    if (this.notificationForm.valid) {
      this.apiService.sendNotification(this.notificationForm.value).subscribe(() => this.router.navigate(['/notifications']));
    }
  }
}