import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { ToolbarModule } from 'primeng/toolbar';
import { ButtonModule } from 'primeng/button';
import { SidebarModule } from 'primeng/sidebar';
import { PanelMenuModule } from 'primeng/panelmenu';
import { AvatarModule } from 'primeng/avatar';
import { BadgeModule } from 'primeng/badge';
import { RippleModule } from 'primeng/ripple';

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
    RippleModule
  ],
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class MainLayoutComponent {
  visibleSidebar: boolean = false;
  primary = '#1E3A5F';
  
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
      label: 'Notifications', 
      icon: 'pi pi-bell', 
      route: '/notifications'
    },
    { 
      label: 'Paramètres', 
      icon: 'pi pi-cog', 
      route: '/settings'
    }
  ];

  userInfo = {
    name: 'Jean Dupont',
    role: 'Administrateur',
    avatar: 'JD'
  };

  constructor(private authService: AuthService, private router: Router) {}

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  closeSidebar() {
    this.visibleSidebar = false;
  }
}