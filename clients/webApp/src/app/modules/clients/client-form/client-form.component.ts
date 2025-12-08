import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-client-form',
  templateUrl: './client-form.component.html',
  styleUrls: ['./client-form.component.scss']
})
export class ClientFormComponent implements OnInit {
  clientForm: FormGroup;
  isEdit = false;
  clientId?: number;

  constructor(
    private fb: FormBuilder, 
    private route: ActivatedRoute, 
    private router: Router, 
    private apiService: ApiService,
    private notificationService: NotificationService
  ) {
    this.clientForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]]
      // Ajoutez plus
    });
  }

  ngOnInit() {
    this.clientId = +this.route.snapshot.paramMap.get('id')!;
    if (this.clientId) {
      this.isEdit = true;
      this.apiService.getClient(this.clientId).subscribe(data => this.clientForm.patchValue(data));
    }
  }

  save() {
    if (this.clientForm.valid) {
      const data = this.clientForm.value;
      if (this.isEdit) {
        this.apiService.updateClient(this.clientId!, data).subscribe((client) => {
          // Notification de modification
          this.notificationService.addNotification({
            type: 'info',
            title: 'Client modifié',
            message: `Les informations du client "${data.nom} ${data.prenom}" ont été mises à jour`,
            read: false,
            action: 'view-client',
            actionData: { clientId: this.clientId }
          });
          this.router.navigate(['/clients']);
        });
      } else {
        this.apiService.createClient(data).subscribe((client) => {
          // Notification de création
          this.notificationService.addNotification({
            type: 'success',
            title: 'Client créé',
            message: `Le client "${data.nom} ${data.prenom}" a été créé avec succès`,
            read: false,
            action: 'view-client',
            actionData: { clientId: client.id }
          });
          this.router.navigate(['/clients']);
        });
      }
    }
  }
}