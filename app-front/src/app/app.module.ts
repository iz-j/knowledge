import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpModule } from '@angular/http';
import { ErrorHandler } from '@angular/core';

import { hogeLibModule } from './../lib/module';

import { AppComponent } from './app.component';
import { AppErrors } from './app.errors';

import { AppRoutingModule } from './app-routing.module';
import { AuthModule } from './auth/auth.module';

import { CustomErrorHandler, CustomHttpResolver, CustomhogeInboxCanClose } from './app.resolvers';

import { ExtendedHttp, HttpResolver } from '../lib/service/http';
import { Logger } from '../lib/base/logger';
import { WindowRef } from '../lib/base/window-ref';
import { hogeInboxCanClose } from '../lib/component/inbox/inbox';

@NgModule({
  imports: [
    BrowserModule,
    HttpModule,
    hogeLibModule,
    AppRoutingModule,
    AuthModule,
  ],
  declarations: [
    AppComponent,
    AppErrors,
  ],
  providers: [
    Logger,
    WindowRef,
    ExtendedHttp,
    { provide: ErrorHandler, useClass: CustomErrorHandler },
    { provide: HttpResolver, useClass: CustomHttpResolver },
    { provide: hogeInboxCanClose, useClass: CustomhogeInboxCanClose }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
