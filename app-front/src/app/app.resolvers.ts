import { Injectable, ErrorHandler, EventEmitter } from '@angular/core';
import { Headers, Response } from "@angular/http";
import { ActivatedRoute } from "@angular/router";
import { Observable } from "rxjs/Observable";
import { HttpResolver } from '../lib/service/http';//'lib/service/http';

import { environment } from '../environments/environment';
import { tenant } from './../resources/tenant';
import { App } from '../app/app.const';
import { Locale } from '../app/app.locales';

import { hogeInboxCanClose } from '../lib/component/inbox/inbox';


export class CustomErrorHandler implements ErrorHandler {
  private event = new EventEmitter<any>();

  get error(): EventEmitter<any> {
    return this.event;
  }

  handleError(error: any) {
    console.error(error);
    this.event.emit(error);
    throw error;
  }
}

export class CustomHttpResolver implements HttpResolver {
  private _unauthorized = new EventEmitter<string>();
  private _conflict = new EventEmitter<string>();
  private _throughputExceed = new EventEmitter<string>();

  get unauthorized(): EventEmitter<string> {
    return this._unauthorized;
  }

  get conflict(): EventEmitter<string> {
    return this._conflict;
  }

  get throughputExceed(): EventEmitter<string> {
    return this._throughputExceed;
  }

  apiRoot(): string {
    return environment.apiRoot;
  }

  mergeHeader(headers: Headers): Headers {
    let jwt = window ? window.localStorage.getItem(App.Storage.TOKEN) : undefined;
    console.log(`Get JWT from storage and put to Request: ${jwt ? jwt.length : null} characters.`);
    if (jwt) {
      headers.append('Authorization', `Bearer ${jwt}`);
    }
    headers.append('Accept-Language', Locale.get());
    headers.append('X-TenantId', tenant.id);

    return headers;
  }

  handleError(error: Response | any): Observable<any> {
    if (!(error instanceof Response)) {
      return Observable.throw(error);
    }

    let errMsg = `Request failed!\nResponseStatus -> ${error.status}, RequestURL -> ${error.url}`;
    console.error(`${errMsg}\n${error.text()}`);
    switch (error.status) {
      case 400:// BadRequest should be handled by caller.
        return Observable.throw(error);
      case 401:// Unauthorized
        this._unauthorized.emit(errMsg);
        return Observable.throw(errMsg);
      case 429:// Throughput exceed
        this._throughputExceed.emit(errMsg);
        return Observable.throw(errMsg);
      case 409:// Conflict
        this._conflict.emit(errMsg);
        return Observable.throw(errMsg);
      case 0:// Zero means connection error.
        alert('Failed to connect to server.');
        return Observable.throw(errMsg);
      default: // The other results are fatal!
        return Observable.throw(errMsg);
    }
  }

  getTimezoneOffset(): number {
    return Number.parseInt(window.localStorage.getItem(App.Storage.TIMEZONE_OFFSET));
  }
}

/**
 * @author ~~~~
 */
export class CustomhogeInboxCanClose implements hogeInboxCanClose {
  public canClose(event: any): boolean {
    if (event.path) {
      return !!event.path.find(p => p.tagName === 'PAGE-ROOT');
    } else {
      let currentElem = event.target;
      while (currentElem) {
        if (!currentElem.parentElement) return false;
        if (currentElem.parentElement.tagName === 'PAGE-ROOT') return true;
        currentElem = currentElem.parentElement;
      }
      return false;
    }
  }
}



