import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { MessageService } from 'primeng/api';
import { NotificationTestComponent } from './notification-test.component';
import { ApiService } from '../../../services/api.service';

describe('NotificationTestComponent', () => {
  let component: NotificationTestComponent;
  let fixture: ComponentFixture<NotificationTestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NotificationTestComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        ApiService,
        MessageService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NotificationTestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
