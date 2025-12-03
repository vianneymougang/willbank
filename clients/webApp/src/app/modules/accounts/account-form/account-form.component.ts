import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';

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
    private apiService: ApiService
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
      if (this.isEdit) {
        this.apiService.updateAccount(this.accountId!, data).subscribe(() => this.router.navigate(['/accounts']));
      } else {
        this.apiService.createAccount(data).subscribe(() => this.router.navigate(['/accounts']));
      }
    }
  }
}
