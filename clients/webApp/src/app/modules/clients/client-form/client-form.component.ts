import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-client-form',
  templateUrl: './client-form.component.html',
  styleUrls: ['./client-form.component.scss']
})
export class ClientFormComponent implements OnInit {
  clientForm: FormGroup;
  isEdit = false;
  clientId?: number;

  constructor(private fb: FormBuilder, private route: ActivatedRoute, private router: Router, private apiService: ApiService) {
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
        this.apiService.updateClient(this.clientId!, data).subscribe(() => this.router.navigate(['/clients']));
      } else {
        this.apiService.createClient(data).subscribe(() => this.router.navigate(['/clients']));
      }
    }
  }
}