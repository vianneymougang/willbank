import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReusableFormComponent } from './reusable-form.component';

describe('ReusableFormComponent', () => {
  let component: ReusableFormComponent;
  let fixture: ComponentFixture<ReusableFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReusableFormComponent],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ReusableFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
