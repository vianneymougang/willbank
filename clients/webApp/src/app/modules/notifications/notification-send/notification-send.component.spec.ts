import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { NotificationSendComponent } from './notification-send.component';
import { ApiService } from '../../../services/api.service';

describe('NotificationSendComponent', () => {
  let component: NotificationSendComponent;
  let fixture: ComponentFixture<NotificationSendComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [NotificationSendComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        ApiService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NotificationSendComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
