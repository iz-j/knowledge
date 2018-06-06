import { Injectable } from '@angular/core';
import { ExtendedHttp } from '../../../../../../lib/service/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class CommentNotificationService {

  constructor(private http: ExtendedHttp) { }

  getNotificationsCount() {
    return this.http.get('/comments/notification/count').map(res => res.text());
  }

  repairNotificationsCount() {
    return this.http.post('/comments/notification/count/repair');
  }

  getNotifications() {
    return this.http.get('/comments/notification').map(res => res.json());
  }

  clearNotifications() {
    return this.http.delete('/comments/notification');
  }

  deleteNotification(notification) {
    return this.http.patch('/comments/notification', {
      notificationId: notification.id,
      notifiedAt: notification.notifiedAt
    });
  }


}