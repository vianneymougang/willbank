import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';
import { NotificationService } from '../../../services/notification.service';

interface TransactionType {
  label: string;
  value: string;
}

@Component({
  selector: 'app-transaction-form',
  templateUrl: './transaction-form.component.html',
  styleUrls: ['./transaction-form.component.scss']
})
export class TransactionFormComponent implements OnInit {
  transactionForm: FormGroup;
  isEdit = false;
  transactionId?: number;

  transactionTypes: TransactionType[] = [
    { label: 'Virement', value: 'virement' },
    { label: 'Dépôt', value: 'depot' },
    { label: 'Retrait', value: 'retrait' }
  ];

  constructor(
    private fb: FormBuilder, 
    private route: ActivatedRoute, 
    private router: Router, 
    private apiService: ApiService,
    private notificationService: NotificationService
  ) {
    this.transactionForm = this.fb.group({
      type: ['', Validators.required],
      montant: [0, [Validators.required, Validators.min(0.01)]],
      telephoneSource: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      telephoneDest: ['', Validators.pattern(/^[0-9]{10}$/)]
    });
  }

  ngOnInit() {
    this.transactionId = +this.route.snapshot.paramMap.get('id')!;
    if (this.transactionId) {
      this.isEdit = true;
      this.apiService.getTransaction(this.transactionId).subscribe(data => this.transactionForm.patchValue(data));
    }
    
    // Écouter les changements du type de transaction
    this.transactionForm.get('type')?.valueChanges.subscribe(type => {
      const telephoneDestControl = this.transactionForm.get('telephoneDest');
      if (type === 'virement') {
        telephoneDestControl?.setValidators([Validators.required, Validators.pattern(/^[0-9]{10}$/)]);
      } else {
        telephoneDestControl?.clearValidators();
        telephoneDestControl?.setValidators(Validators.pattern(/^[0-9]{10}$/));
      }
      telephoneDestControl?.updateValueAndValidity();
    });
  }

  save() {
    if (this.transactionForm.valid) {
      const data = this.transactionForm.value;
      const typeLabel = this.transactionTypes.find(t => t.value === data.type)?.label || data.type;
      
      if (this.isEdit) {
        this.apiService.updateTransaction(this.transactionId!, data).subscribe((transaction) => {
          // Notification de modification
          this.notificationService.addNotification({
            type: 'info',
            title: 'Transaction modifiée',
            message: `La transaction de ${data.montant}€ a été mise à jour`,
            read: false,
            action: 'view-transaction',
            actionData: { transactionId: this.transactionId }
          });
          this.router.navigate(['/transactions']);
        });
      } else {
        this.apiService.createTransfer(data).subscribe((transaction) => {
          // Notification de succès
          this.notificationService.addNotification({
            type: 'success',
            title: 'Transaction effectuée',
            message: `${typeLabel} de ${data.montant}€ effectué avec succès`,
            read: false,
            action: 'view-transaction',
            actionData: { transactionId: transaction.id }
          });
          
          // Notification supplémentaire pour les gros montants
          if (data.montant > 1000) {
            this.notificationService.addNotification({
              type: 'info',
              title: 'Transaction importante',
              message: `Une transaction de ${data.montant}€ a été effectuée`,
              read: false,
              action: 'view-transaction',
              actionData: { transactionId: transaction.id }
            });
          }
          
          this.router.navigate(['/transactions']);
        });
      }
    }
  }
}
