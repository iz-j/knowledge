import { Injectable } from '@angular/core';

import { Observable } from 'rxjs/Observable';

import { ExtendedHttp } from '../../../../../../lib/service/http';//'lib/service/http';


export interface UnreadNotification {
  companyId: string;
  accountId: string;
  partition: number;
  postedAt: string;
  unreadId: string;
  system: boolean;
  url: string;
  fragment: string;
  iconPath: string;
  message: string;
}

@Injectable()
export class UnreadNotificationService {

  constructor(private http: ExtendedHttp) { }

  getUnreadCount(): Observable<number> {
    return this.http.get('/notification/count').map(res => res.json());
  }

  getUnreads(): Observable<Array<UnreadNotification>> {
    return this.http.get('/notification/unreads').map(res => this.resolve(res.json()));
  }

  markAsRead(unreadId: string): void {
    this.http.delete('/notification/unread', { unreadId: unreadId }).subscribe(() => { });
  }

  markAllAsRead(): void {
    this.http.delete('/notification/unreads').subscribe(() => { });
  }

  repairUnread(): void {
    this.http.patch('/notification/repair').subscribe(() => { });
  }

  private resolve(res: Array<UnreadNotification>): Array<UnreadNotification> {
    return res.map(n => {
      let fragmentIndex = n.url.indexOf('#');
      if (fragmentIndex > -1) {
        let src = n.url;
        n.url = src.slice(0, fragmentIndex);
        n.fragment = src.slice(fragmentIndex + 1);
      }
      return n;
    });
  }
}
