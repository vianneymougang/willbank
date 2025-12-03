import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, of, delay } from 'rxjs';
import { environment } from '../../environments/environment';

// Mode mock pour tester sans backend
const MOCK_MODE = false;

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiGatewayUrl;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  login(credentials: { username: string; password: string }): Observable<any> {
    if (MOCK_MODE) {
      // Simuler une connexion réussie
      const mockResponse = { token: 'mock-jwt-token-12345', user: { username: credentials.username } };
      return of(mockResponse).pipe(
        delay(500),
        tap((res: any) => {
          if (isPlatformBrowser(this.platformId)) {
            localStorage.setItem('jwt', res.token);
          }
        })
      );
    }
    
    return this.http.post(`${this.apiUrl}/auth/login`, credentials).pipe(
      tap((res: any) => {
        if (isPlatformBrowser(this.platformId)) {
          localStorage.setItem('jwt', res.token);
        }
      })
    );
  }

  logout() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('jwt');
    }
  }

  isLoggedIn(): boolean {
    if (isPlatformBrowser(this.platformId)) {
      return !!localStorage.getItem('jwt');
    }
    return false;
  }

  forgotPassword(email: string): Observable<any> {
    if (MOCK_MODE) {
      // Simuler l'envoi d'email
      console.log(`Email de réinitialisation envoyé à: ${email}`);
      return of({ message: 'Email envoyé avec succès' }).pipe(delay(1000));
    }
    
    return this.http.post(`${this.apiUrl}/auth/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    if (MOCK_MODE) {
      // Simuler la réinitialisation
      console.log(`Mot de passe réinitialisé avec le token: ${token}`);
      return of({ message: 'Mot de passe réinitialisé avec succès' }).pipe(delay(1000));
    }
    
    return this.http.post(`${this.apiUrl}/auth/reset-password`, { token, newPassword });
  }
}