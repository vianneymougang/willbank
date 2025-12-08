import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { NotificationService } from '../../../services/notification.service';

interface AccountType {
  label: string;
  value: string;
}

@Component({
  selector: 'app-account-form',
  templateUrl: './account-form.component.html',
  styleUrls: ['./account-form.component.scss']
})
export class AccountFormComponent implements OnInit {
  accountForm: FormGroup;
  isEdit = false;
  accountId?: number;

  accountTypes: AccountType[] = [
    { label: 'Compte Courant', value: 'courant' },
    { label: 'Compte Épargne', value: 'epargne' },
    { label: 'Compte Professionnel', value: 'professionnel' }
  ];

  constructor(
    private fb: FormBuilder, 
    private route: ActivatedRoute, 
    private router: Router, 
    private apiService: ApiService,
    private notificationService: NotificationService
  ) {
    this.accountForm = this.fb.group({
      type: ['', Validators.required],
      solde: [0, [Validators.required, Validators.min(0)]],
      clientId: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.accountId = +this.route.snapshot.paramMap.get('id')!;
    if (this.accountId) {
      this.isEdit = true;
      this.apiService.getAccount(this.accountId).subscribe(data => this.accountForm.patchValue(data));
    }
  }

  save() {
    if (this.accountForm.valid) {
      const data = this.accountForm.value;
      const typeLabel = this.accountTypes.find(t => t.value === data.type)?.label || data.type;
      
      if (this.isEdit) {
        this.apiService.updateAccount(this.accountId!, data).subscribe((account) => {
          // Notification de modification
          this.notificationService.addNotification({
            type: 'info',
            title: 'Compte modifié',
            message: `Le compte ${typeLabel} a été mis à jour`,
            read: false,
            action: 'view-account',
            actionData: { accountId: this.accountId }
          });
          this.router.navigate(['/accounts']);
        });
      } else {
        this.apiService.createAccount(data).subscribe((account) => {
          // Notification de création
          this.notificationService.addNotification({
            type: 'success',
            title: 'Nouveau compte',
            message: `Un ${typeLabel} a été créé avec un solde de ${data.solde}€`,
            read: false,
            action: 'view-account',
            actionData: { accountId: account.id }
          });
          
          // Vérifier si le solde est faible
          if (data.solde < 100) {
            this.notificationService.addNotification({
              type: 'warning',
              title: 'Solde faible',
              message: `Le nouveau compte a un solde inférieur à 100€`,
              read: false,
              action: 'view-account',
              actionData: { accountId: account.id }
            });
          }
          
          this.router.navigate(['/accounts']);
        });
      }
    }
  }
}
