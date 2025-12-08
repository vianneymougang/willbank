import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService, Notification } from '../../../services/notification.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html',
  styleUrls: ['./notification-list.component.scss']
})
export class NotificationListComponent implements OnInit {
  notifications$: Observable<Notification[]>;
  unreadCount$: Observable<number>;
  
  filterType: string = 'all'; // all, unread, read
  selectedType: string = 'all'; // all, success, info, warning, error

  constructor(
    private notificationService: NotificationService, 
    private router: Router
  ) {
    this.notifications$ = this.notificationService.notifications$;
    this.unreadCount$ = this.notificationService.unreadCount$;
  }

  ngOnInit() {
    this.notificationService.loadNotifications();
  }

  markAsRead(notificationId: number) {
    this.notificationService.markAsRead(notificationId).subscribe(() => {
      this.notificationService.loadNotifications();
    });
  }

  markAllAsRead() {
    this.notificationService.markAllAsRead().subscribe(() => {
      this.notificationService.loadNotifications();
    });
  }

  deleteNotification(notificationId: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette notification ?')) {
      this.notificationService.deleteNotification(notificationId).subscribe(() => {
        this.notificationService.loadNotifications();
      });
    }
  }

  deleteAllRead() {
    if (confirm('Êtes-vous sûr de vouloir supprimer toutes les notifications lues ?')) {
      this.notificationService.deleteAllRead().subscribe(() => {
        this.notificationService.loadNotifications();
      });
    }
  }

  getNotificationIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'success': 'pi pi-check-circle',
      'info': 'pi pi-info-circle',
      'warning': 'pi pi-exclamation-triangle',
      'error': 'pi pi-times-circle'
    };
    return icons[type] || 'pi pi-bell';
  }

  getTimeAgo(date: string): string {
    const now = new Date();
    const notificationDate = new Date(date);
    const diffMs = now.getTime() - notificationDate.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'À l\'instant';
    if (diffMins < 60) return `Il y a ${diffMins} min`;
    if (diffHours < 24) return `Il y a ${diffHours}h`;
    if (diffDays < 7) return `Il y a ${diffDays}j`;
    return notificationDate.toLocaleDateString('fr-FR');
  }

  send() {
    this.router.navigate(['/notifications/send']);
  }
}