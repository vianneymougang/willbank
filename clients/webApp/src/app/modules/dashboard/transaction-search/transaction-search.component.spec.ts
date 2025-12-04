import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { TransactionSearchComponent } from './transaction-search.component';
import { ApiService } from '../../../services/api.service';

describe('TransactionSearchComponent', () => {
  let component: TransactionSearchComponent;
  let fixture: ComponentFixture<TransactionSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TransactionSearchComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        ApiService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(TransactionSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
