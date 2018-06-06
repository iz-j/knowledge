import { TextUtils } from 'lib/util/text.utils';
import { Component, OnInit, ViewChild, ElementRef, HostListener, Renderer } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Title } from '@angular/platform-browser';

import { WindowRef } from '../../../lib/base/window-ref';
import { App } from '../../app.const';
import { AuthService, ClaimSet } from '../auth.service';
import { Locale } from './../../app.locales';
import { AUTH_TEXT } from './../../../resources/text/auth/auth.text';
import { tenant } from './../../../resources/tenant';
import { environment } from '../../../environments/environment';
import { APP_VERSION } from './../../../environments/version';

const IS_MAC = /Mac|iPad|iPhone|iPod/.test(navigator.platform);

@Component({
  selector: 'login-page',
  templateUrl: './login-page.component.html',
  styleUrls: ['./login-page.component.less']
})
export class LoginPageComponent implements OnInit {
  text: { [key: string]: string; };

  loginForm: FormGroup;
  @ViewChild('email') emailEl: ElementRef;
  @ViewChild('password') passwdEl: ElementRef;

  loginLog: any;

  authenticating: boolean = false;
  authenticationFailedText: string = null;

  capslock: boolean = false;

  langs: Array<any>;
  langPickerActive: boolean = false;

  env: string;
  version: string = APP_VERSION;

  constructor(
    private authService: AuthService,
    private router: Router,
    private title: Title,
    private formBuilder: FormBuilder,
    private elementRef: ElementRef,
    private renderer: Renderer,
    private windowRef: WindowRef,
  ) { }

  ngOnInit() {
    this.text = AUTH_TEXT[Locale.get()];
    this.loginForm = this.formBuilder.group({
      email: ['', Validators.required],
      password: ['', Validators.required],
    });
    this.checkLoginLog();
    this.langs = Locale.getSupportedLocales();
    this.env = environment.name;
  }

  ngAfterViewInit() {
    this.title.setTitle(tenant.serviceName);
    this.renderer.setElementStyle(this.windowRef.nativeWindow.document.body, 'overflow-y', 'auto');

    if (this.loginLog) {
      this.passwdEl.nativeElement.focus();
    } else {
      this.emailEl.nativeElement.focus();
    }
  }

  onLoginSubmit(event) {
    event.preventDefault();

    if (this.authenticating) {
      return;
    }

    if (this.loginForm.invalid) {
      this.loginForm.markAsTouched();
      return;
    }

    let value = this.loginForm.value;
    this.authenticating = true;
    this.authService.login(value.email, value.password, Locale.get()).subscribe(
      res => {
        if (res.errorCode) {
          this.authenticating = false;
          if (res.errorCode === 'LOGIN_TRIAL_LIMIT_APPROACHING') {
            this.authenticationFailedText = TextUtils.format(this.text[`msg_${res.errorCode.toLowerCase()}`], res.remainingTrial);
          } else {
            this.authenticationFailedText = this.text[`msg_${res.errorCode.toLowerCase()}`];
          }
        } else {
          this.saveLoginLog();
          this.router.navigate([this.authService.redirectUrl]);
        }
      },
      err => {
        this.authenticating = false;
        this.authenticationFailedText = this.text['msg_unknown_error'];
      });
  }

  clearLoginLog() {
    this.loginLog = null;
    this.loginForm.get('email').setValue('');
  }

  onKeyPress(e) {
    let code = e.charCode || e.keyCode;
    let shiftKey = e.shiftKey;
    let priorCapsLock = this.capslock;

    if (code >= 97 && code <= 122) this.capslock = shiftKey;
    if (code >= 65 && code <= 90 && !(shiftKey && IS_MAC)) this.capslock = !shiftKey;
  }

  onLangClick(localeId) {
    Locale.set(localeId);
  }

  @HostListener('document:click', ['$event.target'])
  hideLangPicker(targetElement) {
    if (!this.langPickerActive) {
      return;
    }
    if (!this.elementRef.nativeElement.querySelector('.lang-picker').contains(targetElement)) {
      this.langPickerActive = false;
    }
  }

  private checkLoginLog() {
    try {
      let log = this.windowRef.nativeWindow.localStorage.getItem(App.Storage.LOGIN_LOG);
      if (!log) {
        return;
      }

      log = JSON.parse(log);
      if (!log.email || !log.company || !log.person || !log.avatar || !log.logo) {
        return;
      }

      this.loginLog = log;
      if (this.loginLog.person.length > 30) {
        var nameList = log.person.split(' ');
        this.loginLog.person = nameList[0];
      }
      this.loginForm.get('email').setValue(this.loginLog.email);

    } catch (e) {
      this.windowRef.nativeWindow.localStorage.removeItem(App.Storage.LOGIN_LOG);
    }
  }

  private saveLoginLog() {
    let claim: ClaimSet = this.authService.claim;
    let log = {
      email: claim.email,
      company: claim.companyName,
      person: claim.lastName + ' ' + claim.firstName,
      avatar: claim.avatar,
      logo: claim.companyIcon,
    };
    this.windowRef.nativeWindow.localStorage.setItem(App.Storage.LOGIN_LOG, JSON.stringify(log));
  }

}
