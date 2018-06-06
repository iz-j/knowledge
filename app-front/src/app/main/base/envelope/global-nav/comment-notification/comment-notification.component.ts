import { Component, OnInit, OnDestroy, HostListener, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { CommentNotificationService } from './comment-notification.service';
import { COMMENT_TEXT } from '../../../../../../resources/text/component/component.text';
import { Locale } from './../../../../../app.locales';
import { Subscription } from 'rxjs/Subscription';
import { Observable } from 'rxjs/Observable';
import { AuthService } from '../../../../../auth/auth.service';
import 'rxjs/add/observable/timer';
import { take } from 'rxjs/operator/take';

@Component({
  selector: 'comment-notification',
  templateUrl: 'comment-notification.component.html',
  styleUrls: ['comment-notification.component.less']
})

export class CommentNotificationComponent implements OnInit, OnDestroy {
  notificationsCount: number;
  notifications: Array<any> = [];
  text: { key: string, string };
  popoverActive: boolean;
  loaded: boolean;
  timer: Subscription;
  locale: string;
  timezoneOffset: number;

  constructor(private service: CommentNotificationService,
    private elementRef: ElementRef,
    private router: Router,
    private auth: AuthService
  ) { }

  ngOnInit() {
    this.timer = Observable.timer(1000, 180000).subscribe(() => {
      this.service.getNotificationsCount().subscribe(res => {
        this.notificationsCount = +res;
      });
    });
    this.locale = Locale.get();
    this.timezoneOffset = this.auth.claim.timezoneOffset;
    this.text = COMMENT_TEXT[Locale.get()];
  }

  ngOnDestroy() {
    if (this.timer) {
      this.timer.unsubscribe();
      console.log('Polling unread count of comment notifications stopped.');
    }
  }

  togglePopover() {
    this.popoverActive = !this.popoverActive;
    if (this.popoverActive && this.notifications.length < this.notificationsCount) {
      this.service.getNotifications().subscribe(res => {
        this.notifications = res;
        let diff = this.notificationsCount != this.notifications.length;
        if (diff) {
          this.notificationsCount = this.notifications.length;
          this.service.repairNotificationsCount().subscribe();
        }
        this.loaded = true;
      });
    } else {
      this.loaded = true;
    }
  }

  onNotificationClick(n) {
    this.router.navigate([n.url]);
    this._removeNotification(n);
  }

  onClearClick(event, n) {
    this._removeNotification(n);
  }

  private _removeNotification(n) {
    this.service.deleteNotification(n).subscribe(res => {
      this.notificationsCount--;
      this.notifications = this.notifications.filter(notification => notification !== n);
    });
  }



  @HostListener('document:click', ['$event.target'])
  hidePopover(targetElement: Element) {
    if (this.elementRef.nativeElement.querySelector('.action').contains(targetElement)) {
      return;
    }
    let containsPopover = this.elementRef.nativeElement.querySelector('.popover').contains(targetElement);
    if (!containsPopover) {
      this.popoverActive = false;
    } else {
      let clearCommentEls = Array.prototype.slice.call(this.elementRef.nativeElement.querySelectorAll('i.clear-comment'));
      if (clearCommentEls.length && clearCommentEls.find(el => el === targetElement)) {
        this.popoverActive = true;
      } else {
        let notificationEl = this.elementRef.nativeElement.querySelector('.notifications');
        this.popoverActive = !notificationEl || notificationEl.contains(targetElement);
      }
    }
  }
}

