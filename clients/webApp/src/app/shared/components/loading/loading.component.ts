import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoadingService } from '../../../services/loading.service';
import { ProgressSpinnerModule } from 'primeng/progressspinner';

@Component({
  selector: 'app-loading',
  standalone: true,
  imports: [CommonModule, ProgressSpinnerModule],
  template: `
    <div class="loading-overlay" *ngIf="loadingService.loading$ | async">
      <div class="loading-container">
        <p-progressSpinner 
          styleClass="custom-spinner"
          strokeWidth="4"
          animationDuration="1s">
        </p-progressSpinner>
        <p class="loading-text">Chargement en cours...</p>
      </div>
    </div>
  `,
  styles: [`
    .loading-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      justify-content: center;
      align-items: center;
      z-index: 9999;
    }

    .loading-container {
      text-align: center;
      background: white;
      padding: 2rem;
      border-radius: 8px;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }

    .loading-text {
      margin-top: 1rem;
      color: #333;
      font-size: 1rem;
      font-weight: 500;
    }

    ::ng-deep .custom-spinner {
      width: 60px;
      height: 60px;
    }
  `]
})
export class LoadingComponent {
  constructor(public loadingService: LoadingService) {}
}
