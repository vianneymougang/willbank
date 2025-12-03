import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-reusable-form',
  templateUrl: './reusable-form.component.html',
  styleUrls: ['./reusable-form.component.scss']
})
export class ReusableFormComponent implements OnInit {
  @Input() fields: { name: string; label: string; type?: string; required?: boolean }[] = [];
  form!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    const controls: any = {};
    this.fields.forEach(field => {
      controls[field.name] = [ '', field.required ? Validators.required : null ];
    });
    this.form = this.fb.group(controls);
  }
}