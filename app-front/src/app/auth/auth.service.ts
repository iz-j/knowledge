import { Injectable, EventEmitter } from '@angular/core';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/finally';
import 'rxjs/add/observable/of';

import { ExtendedHttp } from '../../lib/service/http';
import { WindowRef } from '../../lib/base/window-ref';
import { environment } from '../../environments/environment';
import { App } from '../../app/app.const';


const PORTAL_URL = '/main';

export interface ClaimSet {
  iss: string;
  sub: string;// accountId
  iat: number;
  exp: number;
  email: string;
  companyId: string;
  companyName: string;
  companyIcon: string;
  deptName: string;
  firstName: string;
  lastName: string;
  fullName: string;
  avatar: string;
  timezoneOffset: number;
  introduced: boolean;
  host: boolean;
  admin: boolean;
}

@Injectable()
export class AuthService {
  private jwt: string;
  private claimSet: ClaimSet;
  private onRefresh = new EventEmitter<ClaimSet>();

  // store the URL so we can redirect after logging in
  redirectUrl: string = PORTAL_URL;

  get claim(): ClaimSet {
    if (!this.jwt) {
      this.checkStorage();
    }
    return this.claimSet;
  }

  get isAuthenticated(): boolean {
    if (!this.jwt) {
      this.checkStorage();
    }
    return !!this.jwt;
  }

  get refreshEvent(): EventEmitter<ClaimSet> {
    return this.onRefresh;
  }

  constructor(
    private windowRef: WindowRef,
    private http: ExtendedHttp,
  ) { }

  login(email: string, password: string, locale: string): Observable<any> {
    return this.http
      .post('/authentication/login', { email: email, password: password, locale: locale })
      .map(res => {
        let result = res.json();
        if (result.token && !this.checkResponse(result.token)) {
          result.errorCode = 'INVALID_TOKEN';
        }
        return result;
      });
  }

  refresh(): void {
    this.http.post('/authentication/refresh', null).subscribe(res => {
      this.checkResponse(res.text());
      this.onRefresh.emit(this.claimSet);
    });
  }

  logout(): Observable<boolean> {
    return this.http
      .post('/authentication/logout', null).map(res => {
        return true;
      }).finally(() => {
        this.jwt = null;
        this.claimSet = null;
        this.windowRef.nativeWindow.localStorage.removeItem(App.Storage.TOKEN);
        this.redirectUrl = PORTAL_URL;
      });
  }

  requestResetPassword(email: string): Observable<boolean> {
    return this.http
      .post('/password-reset/request', { email: email }).map(res => {
        return true;
      });
  }

  validateResetToken(token: string): Observable<boolean> {
    return this.http
      .get('/password-reset/request', { token: token }).map(res => {
        return res.json();
      });
  }

  resetPassword(token: string, password: string, passwordForVerify: string): Observable<boolean> {
    return this.http
      .post('/password-reset/input', {
        token: token,
        password: password,
        passwordForVerify: passwordForVerify
      }).map(res => {
        return true;
      }).catch(err => {
        return Observable.throw(err.json());
      });
  }

  private checkResponse(token: string): boolean {
    if (!this.extractClaim(token)) {
      return false;
    }
    this.jwt = token;
    console.log(`Set JWT to storage: ${this.jwt ? this.jwt.length : null} length.`);
    this.windowRef.nativeWindow.localStorage.setItem(App.Storage.TOKEN, token);
    this.windowRef.nativeWindow.localStorage.setItem(App.Storage.TIMEZONE_OFFSET, this.claim.timezoneOffset);
    return true;
  }

  private checkStorage(): boolean {
    let jwt = this.windowRef.nativeWindow.localStorage.getItem(App.Storage.TOKEN);
    if (!this.extractClaim(jwt)) {
      return false;
    }
    this.jwt = jwt;
    console.log('Restore claim from localStorage.');
    return true;
  }

  private extractClaim(jwt: string): boolean {
    if (!jwt) {
      console.warn('jwt required!');
      return false;
    }

    let claimPart = jwt.split('.')[1];
    if (!claimPart) {
      console.warn('jwt invalid!');
      return false;
    }

    try {
      let claimJson = decodeURIComponent(this.windowRef.nativeWindow.atob(claimPart));
      this.claimSet = JSON.parse(claimJson);

      // To avoid avatar cache.
      this.claimSet.avatar += `?${+new Date()}`;
    } catch (e) {
      console.error('Failed to parse token!', e);
      throw e;
    }
    return true;
  }
}
