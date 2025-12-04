import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { StatementComponent } from './statement.component';
import { ApiService } from '../../../services/api.service';

describe('StatementComponent', () => {
  let component: StatementComponent;
  let fixture: ComponentFixture<StatementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StatementComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        ApiService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StatementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
