import { Component, OnInit, HostListener, ElementRef, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';

import { Subscription } from 'rxjs/Subscription';
import { Observable } from 'rxjs/Observable';
import { AuthService } from '../../../../../auth/auth.service';

import { Locale } from './../../../../../app.locales';
import { BASE_TEXT } from './../../../../../../resources/text/base/base.text';
import { UnreadNotification, UnreadNotificationService } from './unread-notification.service';


@Component({
  selector: 'notification',
  templateUrl: './notification.component.html',
  styleUrls: ['./notification.component.less']
})
export class NotificationComponent implements OnInit, OnDestroy {
  text: { [key: string]: string; };

  popoverActive: boolean = false;
  timer: Subscription;

  unreadCount: number = undefined;
  loaded: boolean = false;
  notifications: Array<UnreadNotification> = [];
  locale: string;
  timezoneOffset: number;

  constructor(
    private elementRef: ElementRef,
    private service: UnreadNotificationService,
    private auth: AuthService
  ) { }

  ngOnInit() {
    this.locale = Locale.get();
    this.text = BASE_TEXT[Locale.get()];
    this.timezoneOffset = this.auth.claim.timezoneOffset;
    this.timer = Observable.timer(1000, 180000).subscribe(() => this.getUnreadCount());
  }

  ngOnDestroy() {
    if (this.timer) {
      this.timer.unsubscribe();
      console.log('Polling unread count of notifications stopped.');
    }
  }

  private getUnreadCount() {
    if (this.popoverActive) {
      console.log('Popover is active, so skip polling unread count of notifications.');
    } else {
      this.service.getUnreadCount().subscribe(res => {
        console.log('Unread count of notifications has been retrieved.');
        this.unreadCount = res;
      });
    }
  }

  togglePopover() {
    // Prevent open before initialized.
    if (this.unreadCount === undefined) {
      return;
    }

    this.popoverActive = !this.popoverActive;

    if (this.popoverActive && !this.loaded) {
      this.service.getUnreads().subscribe(res => {
        this.loaded = true;
        this.notifications = res;
        if (this.notifications.length != this.unreadCount) {
          // Repair unread count if mismatch.
          this.service.repairUnread();
          this.unreadCount = this.notifications.length;
        }
      });
    }
  }

  @HostListener('document:click', ['$event.target'])
  hidePopover(targetElement) {
    if (!this.popoverActive) {
      return;
    }
    if (this.elementRef.nativeElement.querySelector('.action').contains(targetElement)) {
      return;
    }
    this.popoverActive = this.elementRef.nativeElement.querySelector('.popover').contains(targetElement);
  }

  onAnchorClick(notification) {
    this.markAsRead(notification);
  }

  onClearClick(event, notification) {
    // Mark as read without page transition.
    event.preventDefault();
    event.stopPropagation();
    this.markAsRead(notification);
  }

  private markAsRead(notification) {
    this.notifications = this.notifications.filter(n => n.unreadId !== notification.unreadId);
    this.unreadCount--;
    this.service.markAsRead(notification.unreadId);
  }

  onClearAllClick() {
    if (this.notifications.length > 0) {
      this.notifications = [];
      this.unreadCount = 0;
      this.service.markAllAsRead();
    }
  }

}
