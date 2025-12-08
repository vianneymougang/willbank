import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AuthService } from '../services/auth.service';
import { NotificationService, Notification } from '../services/notification.service';
import { Router } from '@angular/router';
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { SidebarModule } from 'primeng/sidebar';
import { PanelMenuModule } from 'primeng/panelmenu';
import { AvatarModule } from 'primeng/avatar';
import { BadgeModule } from 'primeng/badge';
import { RippleModule } from 'primeng/ripple';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { LoadingComponent } from '../shared/components/loading/loading.component';
import { Observable } from 'rxjs';

interface NavItem {
  label: string;
  icon: string;
  route: string;
  badge?: string;
  badgeClass?: string;
}

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    CommonModule, 
    RouterOutlet, 
    RouterLink,
    RouterLinkActive,
    ToolbarModule, 
    ButtonModule, 
    SidebarModule, 
    PanelMenuModule,
    AvatarModule,
    BadgeModule,
    RippleModule,
    ToastModule,
    TooltipModule,
    LoadingComponent
  ],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  visibleSidebar: boolean = false;
  notificationsPanelVisible: boolean = false;
  primary = '#1E3A5F';
  
  notifications$: Observable<Notification[]>;
  unreadCount$: Observable<number>;
  
  navItems: NavItem[] = [
    { 
      label: 'Tableau de bord', 
      icon: 'pi pi-home', 
      route: '/dashboard'
    },
    { 
      label: 'Clients', 
      icon: 'pi pi-users', 
      route: '/clients',
      badge: '5',
      badgeClass: 'badge-success'
    },
    { 
      label: 'Comptes', 
      icon: 'pi pi-wallet', 
      route: '/accounts'
    },
    { 
      label: 'Transactions', 
      icon: 'pi pi-money-bill', 
      route: '/transactions',
      badge: '3',
      badgeClass: 'badge-warning'
    },
    { 
      label: 'Déconnexion', 
      icon: 'pi pi-cog', 
      route: '/settings'
    }
  ];

  userInfo = {
    name: 'Jean Dupont',
    role: 'Administrateur',
    avatar: 'JD'
  };

  constructor(
    private authService: AuthService, 
    private router: Router,
    private notificationService: NotificationService
  ) {
    this.notifications$ = this.notificationService.notifications$;
    this.unreadCount$ = this.notificationService.unreadCount$;
  }

  ngOnInit() {
    // Charger les notifications au démarrage
    this.notificationService.loadNotifications();
  }

  ngOnDestroy() {
    // Arrêter le polling lors de la destruction du composant
    this.notificationService.stopPolling();
  }

  toggleNotifications() {
    this.notificationsPanelVisible = !this.notificationsPanelVisible;
  }

  getUnreadCountString(): string {
    let count = 0;
    this.unreadCount$.subscribe(c => count = c).unsubscribe();
    return count > 0 ? count.toString() : '';
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
    this.notificationService.deleteNotification(notificationId).subscribe(() => {
      this.notificationService.loadNotifications();
    });
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

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  closeSidebar() {
    this.visibleSidebar = false;
  }
}