import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, interval, of, delay } from 'rxjs';
import { switchMap, catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

// Mode mock pour tester sans backend
const MOCK_MODE = true;

export interface Notification {
  id: number;
  type: 'success' | 'info' | 'warning' | 'error';
  title: string;
  message: string;
  date: string;
  read: boolean;
  action?: string;
  actionData?: any;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private baseUrl = `${environment.apiGatewayUrl}/api/v1/notifications`;
  
  // BehaviorSubject pour partager les notifications entre tous les composants
  private notificationsSubject = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notificationsSubject.asObservable();
  
  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();

  // Données mockées pour le développement
  private mockNotifications: Notification[] = [
    {
      id: 1,
      type: 'success',
      title: 'Client créé',
      message: 'Le client "Jean Dupont" a été créé avec succès',
      date: new Date().toISOString(),
      read: false,
      action: 'view-client',
      actionData: { clientId: 1 }
    },
    {
      id: 2,
      type: 'info',
      title: 'Nouveau compte',
      message: 'Un nouveau compte courant a été ouvert pour Marie Martin',
      date: new Date(Date.now() - 3600000).toISOString(),
      read: false,
      action: 'view-account',
      actionData: { accountId: 3 }
    },
    {
      id: 3,
      type: 'success',
      title: 'Transaction effectuée',
      message: 'Virement de 500€ effectué avec succès',
      date: new Date(Date.now() - 7200000).toISOString(),
      read: true,
      action: 'view-transaction',
      actionData: { transactionId: 4 }
    },
    {
      id: 4,
      type: 'warning',
      title: 'Solde faible',
      message: 'Le compte de Pierre Bernard a un solde inférieur à 100€',
      date: new Date(Date.now() - 10800000).toISOString(),
      read: false,
      action: 'view-account',
      actionData: { accountId: 4 }
    }
  ];

  private pollingSubscription: any;

  constructor(private http: HttpClient) {
    this.loadNotifications();
    this.startPolling();
  }

  /**
   * Charge toutes les notifications
   */
  loadNotifications(): void {
    this.getNotifications().subscribe(notifications => {
      this.notificationsSubject.next(notifications);
      this.updateUnreadCount(notifications);
    });
  }

  /**
   * Récupère les notifications depuis le backend ou mock
   */
  getNotifications(): Observable<Notification[]> {
    if (MOCK_MODE) {
      return of([...this.mockNotifications]).pipe(delay(300));
    }
    return this.http.get<Notification[]>(this.baseUrl).pipe(
      catchError(error => {
        console.error('Erreur lors du chargement des notifications:', error);
        return of([]);
      })
    );
  }

  /**
   * Récupère les notifications non lues
   */
  getUnreadNotifications(): Observable<Notification[]> {
    if (MOCK_MODE) {
      const unread = this.mockNotifications.filter(n => !n.read);
      return of([...unread]).pipe(delay(300));
    }
    return this.http.get<Notification[]>(`${this.baseUrl}/unread`).pipe(
      catchError(error => {
        console.error('Erreur lors du chargement des notifications non lues:', error);
        return of([]);
      })
    );
  }

  /**
   * Marque une notification comme lue
   */
  markAsRead(notificationId: number): Observable<void> {
    if (MOCK_MODE) {
      const notification = this.mockNotifications.find(n => n.id === notificationId);
      if (notification) {
        notification.read = true;
        this.loadNotifications();
      }
      return of(void 0).pipe(delay(200));
    }
    return this.http.put<void>(`${this.baseUrl}/${notificationId}/read`, {}).pipe(
      catchError(error => {
        console.error('Erreur lors du marquage de la notification:', error);
        return of(void 0);
      })
    );
  }

  /**
   * Marque toutes les notifications comme lues
   */
  markAllAsRead(): Observable<void> {
    if (MOCK_MODE) {
      this.mockNotifications.forEach(n => n.read = true);
      this.loadNotifications();
      return of(void 0).pipe(delay(200));
    }
    return this.http.put<void>(`${this.baseUrl}/read-all`, {}).pipe(
      catchError(error => {
        console.error('Erreur lors du marquage de toutes les notifications:', error);
        return of(void 0);
      })
    );
  }

  /**
   * Supprime une notification
   */
  deleteNotification(notificationId: number): Observable<void> {
    if (MOCK_MODE) {
      const index = this.mockNotifications.findIndex(n => n.id === notificationId);
      if (index !== -1) {
        this.mockNotifications.splice(index, 1);
        this.loadNotifications();
      }
      return of(void 0).pipe(delay(200));
    }
    return this.http.delete<void>(`${this.baseUrl}/${notificationId}`).pipe(
      catchError(error => {
        console.error('Erreur lors de la suppression de la notification:', error);
        return of(void 0);
      })
    );
  }

  /**
   * Supprime toutes les notifications lues
   */
  deleteAllRead(): Observable<void> {
    if (MOCK_MODE) {
      this.mockNotifications = this.mockNotifications.filter(n => !n.read);
      this.loadNotifications();
      return of(void 0).pipe(delay(200));
    }
    return this.http.delete<void>(`${this.baseUrl}/read`).pipe(
      catchError(error => {
        console.error('Erreur lors de la suppression des notifications lues:', error);
        return of(void 0);
      })
    );
  }

  /**
   * Ajoute une nouvelle notification (pour simuler les événements système)
   */
  addNotification(notification: Omit<Notification, 'id' | 'date'>): void {
    if (MOCK_MODE) {
      const newNotification: Notification = {
        ...notification,
        id: Math.max(...this.mockNotifications.map(n => n.id), 0) + 1,
        date: new Date().toISOString()
      };
      this.mockNotifications.unshift(newNotification);
      this.loadNotifications();
    }
  }

  /**
   * Démarre le polling pour récupérer les nouvelles notifications
   * Vérifie toutes les 30 secondes
   */
  private startPolling(): void {
    this.pollingSubscription = interval(30000) // 30 secondes
      .pipe(
        switchMap(() => this.getNotifications())
      )
      .subscribe(notifications => {
        this.notificationsSubject.next(notifications);
        this.updateUnreadCount(notifications);
      });
  }

  /**
   * Arrête le polling
   */
  stopPolling(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }

  /**
   * Met à jour le compteur de notifications non lues
   */
  private updateUnreadCount(notifications: Notification[]): void {
    const unreadCount = notifications.filter(n => !n.read).length;
    this.unreadCountSubject.next(unreadCount);
  }

  /**
   * Récupère le nombre de notifications non lues
   */
  getUnreadCount(): number {
    return this.unreadCountSubject.value;
  }
}
