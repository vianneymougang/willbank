import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { NotificationService } from '../services/notification.service';

@Injectable()
export class NotificationResponseInterceptor implements HttpInterceptor {
  
  constructor(private notificationService: NotificationService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      tap(event => {
        if (event instanceof HttpResponse) {
          const body: any = event.body;
          
          // Vérifier si la réponse contient une notification unique
          if (body && body.notification) {
            this.notificationService.addNotification({
              type: body.notification.type,
              title: body.notification.title,
              message: body.notification.message,
              read: false,
              action: body.notification.action,
              actionData: body.notification.actionData
            });
          }
          
          // Vérifier si la réponse contient plusieurs notifications
          if (body && body.notifications && Array.isArray(body.notifications)) {
            body.notifications.forEach((notif: any) => {
              this.notificationService.addNotification({
                type: notif.type,
                title: notif.title,
                message: notif.message,
                read: false,
                action: notif.action,
                actionData: notif.actionData
              });
            });
          }
        }
      })
    );
  }
}
