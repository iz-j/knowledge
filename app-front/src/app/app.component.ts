import { Component, OnInit, ErrorHandler, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

import { HttpResolver } from '../lib/service/http';
import { CustomErrorHandler, CustomHttpResolver } from './app.resolvers';
import { AppErrors } from './app.errors';
import { Locale } from './app.locales';
import { APP_TEXT } from './../resources/text/app.text';

@Component({
  selector: 'app-root',
  template: `
    <ng-container>
      <router-outlet></router-outlet>
      <app-errors></app-errors>
      <hoge-snackbar
        type="error" [(active)]="snackbarActive" [text]="texts[error]"
        [actionLabel]="texts['reload']" (action)="reload()">
      </hoge-snackbar>
    </ng-container>
  `,
  styles: [`
    .unsupported{ padding: 8px;}
  `]
})
export class AppComponent implements OnInit {

  @ViewChild(AppErrors) appErrors: AppErrors;

  texts: any;
  snackbarActive: boolean;
  error: string;
  unsupported: boolean;

  constructor(
    private router: Router,
    private errorHandler: ErrorHandler,
    private httpResolver: HttpResolver,
  ) { }

  ngOnInit() {
    this.texts = APP_TEXT[Locale.get()];

    let customErrorHandler = this.errorHandler as CustomErrorHandler;
    customErrorHandler.error.subscribe(error => {
      this.appErrors.push(error);
    });

    let customHttpResolver = this.httpResolver as CustomHttpResolver;
    customHttpResolver.unauthorized.subscribe(error => {
      console.warn('Request was unauthorized, so transfer to login page.');
      this.router.navigate(['/login']);
    });
    customHttpResolver.conflict.subscribe(error => {
      console.warn('Request was conflicted.');
      this.error = 'conflict_error';
      this.snackbarActive = true;
    });
    customHttpResolver.throughputExceed.subscribe(error => {
      console.warn('Exceed provision throughput.');
      this.error = 'throughput_error';
      this.snackbarActive = true;
    });

  }

  reload() {
    location.reload();
  }

}
