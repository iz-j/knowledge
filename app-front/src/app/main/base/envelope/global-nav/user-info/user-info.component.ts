import { Component, OnInit, OnDestroy, HostListener, ElementRef } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';
import * as html2canvas from 'html2canvas';
import { Locale } from './../../../../../app.locales';
import { BASE_TEXT } from './../../../../../../resources/text/base/base.text';
import { AuthService, ClaimSet } from '../../../../../../app/auth/auth.service';

@Component({
  selector: 'user-info',
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.less']
})
export class UserInfoComponent implements OnInit, OnDestroy {
  text: { [key: string]: string; };

  popoverActive: boolean = false;
  profile: any = {
    avatar: '',
    fullName: '',
    companyName: '',
    deptName: '',
  };

  private authSub: Subscription;
  private routerSub: Subscription;

  constructor(
    private elementRef: ElementRef,
    private router: Router,
    private auth: AuthService,
  ) { }

  ngOnInit() {
    this.text = BASE_TEXT[Locale.get()];

    this.loadProfile(this.auth.claim);
    this.authSub = this.auth.refreshEvent
      .subscribe(claim => this.loadProfile(claim));

    this.routerSub = this.router.events
      .filter(event => event instanceof NavigationEnd)
      .subscribe(event => {
        this.popoverActive = false;
      });
  }

  ngOnDestroy() {
    this.authSub.unsubscribe();
    this.routerSub.unsubscribe();
  }

  loadProfile(claim: ClaimSet) {
    this.profile.avatar = claim.avatar;
    this.profile.fullName = claim.fullName;
    this.profile.companyName = claim.companyName;
    this.profile.deptName = claim.deptName;
  }

  @HostListener('document:click', ['$event.target'])
  hidePopovers(targetElement) {
    if (!this.popoverActive) {
      return;
    }
    if (this.elementRef.nativeElement.querySelector('.user-avatar').contains(targetElement)) {
      return;
    }
    this.popoverActive = this.elementRef.nativeElement.querySelector('.user-popover').contains(targetElement);
  }

  onLogoutClick() {
    this.auth.logout().subscribe(success => {
      this.router.navigate(['/login']);
    });
  }

  testHtml2Canvas() {
    html2canvas(document.body).then((canvas) => {
      let src = canvas.toDataURL("image/jpg");
      let win = window.open('', '_blank');
      if (win) {
        let img = win.document.createElement('img');
        img.src = src;
        win.document.body.appendChild(img);
      }
    });
  }
}
