import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MessageService } from 'primeng/api';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  
  constructor(private messageService: MessageService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Une erreur est survenue';

        if (error.error instanceof ErrorEvent) {
          // Erreur côté client
          errorMessage = `Erreur: ${error.error.message}`;
        } else {
          // Erreur côté serveur
          switch (error.status) {
            case 400:
              errorMessage = 'Requête invalide';
              break;
            case 401:
              errorMessage = 'Non autorisé. Veuillez vous reconnecter';
              break;
            case 403:
              errorMessage = 'Accès refusé';
              break;
            case 404:
              errorMessage = 'Ressource non trouvée';
              break;
            case 500:
              errorMessage = 'Erreur serveur. Veuillez réessayer plus tard';
              break;
            default:
              errorMessage = `Erreur ${error.status}: ${error.message}`;
          }
        }

        // Afficher le message d'erreur
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: errorMessage,
          life: 5000
        });

        return throwError(() => error);
      })
    );
  }
}
