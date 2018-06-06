import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { hogeLibModule } from '../../../../lib/module';
import { ServiceModule } from '../../service/service.module';

import { EnvelopeComponent } from './envelope.component';

import { GlobalNavComponent } from './global-nav/global-nav.component';
import { MenuListComponent } from './menu-list/menu-list.component';
import { NotificationComponent } from './global-nav/notification/notification.component';
import { UserInfoComponent } from './global-nav/user-info/user-info.component';
import { CommentNotificationComponent } from './global-nav/comment-notification/comment-notification.component';

import { UnreadNotificationService } from './global-nav/notification/unread-notification.service';
import { CommentNotificationService } from './global-nav/comment-notification/comment-notification.service';
import { MenuListService } from './menu-list/menu-list.service';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    hogeLibModule,
    ServiceModule
  ],
  declarations: [
    EnvelopeComponent,
    GlobalNavComponent,
    NotificationComponent,
    UserInfoComponent,
    MenuListComponent,
    CommentNotificationComponent
  ],
  providers: [
    UnreadNotificationService,
    CommentNotificationService,
    MenuListService,
  ],
  exports: [
    GlobalNavComponent,
    MenuListComponent
  ]
})
export class EnvelopeModule { }
