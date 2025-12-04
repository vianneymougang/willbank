import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReusableTableComponent } from './reusable-table.component';

describe('ReusableTableComponent', () => {
  let component: ReusableTableComponent;
  let fixture: ComponentFixture<ReusableTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReusableTableComponent],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ReusableTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
