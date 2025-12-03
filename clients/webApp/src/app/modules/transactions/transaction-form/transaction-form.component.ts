import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';

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
    private apiService: ApiService
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
      if (this.isEdit) {
        this.apiService.updateTransaction(this.transactionId!, data).subscribe(() => this.router.navigate(['/transactions']));
      } else {
        this.apiService.createTransfer(data).subscribe(() => this.router.navigate(['/transactions']));
      }
    }
  }
}
