import { Component, OnInit, OnDestroy, ElementRef, Renderer, HostListener, ChangeDetectorRef } from '@angular/core';
import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { Subscription } from 'rxjs/Subscription';
import { WindowRef } from './../../../../lib/base/window-ref';
import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/mergeMap';

import { TITLE_TEXT } from './../../../../resources/text/title.text';
import { Locale } from './../../../app.locales';

@Component({
  selector: 'envelope',
  templateUrl: './envelope.component.html',
  styleUrls: ['./envelope.component.less']
})
export class EnvelopeComponent implements OnInit, OnDestroy {

  pageTitle: string;
  pageType: string;// portal, normal, search

  scrollTop: number = 0;
  layout: string = null;
  menuModal: boolean;

  private scrollingElement: Element;
  private onMediaChange: MediaQueryListListener;
  private mediaQueryList: MediaQueryList;
  private sub: Subscription;

  constructor(
    private elementRef: ElementRef,
    private renderer: Renderer,
    private changeDetectorRef: ChangeDetectorRef,
    private router: Router,
    private route: ActivatedRoute,
    private title: Title,
    private windowRef: WindowRef,
  ) { }

  ngOnInit() {
    // Setup title & pageType.
    this.setupFromRouteData(this.toFirstChildRoute(this.route).snapshot.data);

    // Always show scrollbar.
    this.renderer.setElementStyle(this.windowRef.nativeWindow.document.body, 'overflow-y', 'scroll');

    // Reset scroll position & determine menu-list style when url changed.
    // And setup title & pageType.
    this.sub = this.router.events
      .filter(event => event instanceof NavigationEnd)
      .map(() => this.route)
      .map(route => this.toFirstChildRoute(route))
      .filter(route => route.outlet === 'primary')
      .mergeMap(route => route.data)
      .subscribe(data => {
        this.resetScorllTop();
        this.checkPageLayout();
        this.setupFromRouteData(data);
      });


    // To adjust menu-list style.
    this.onMediaChange = this.setMenuStyle.bind(this);
    this.mediaQueryList = this.windowRef.nativeWindow.matchMedia('(min-width:1360px)');
    this.mediaQueryList.addListener(this.onMediaChange);
    this.checkPageLayout();
  }

  ngOnDestroy() {
    this.changeDetectorRef.detach();
    this.scrollingElement = null;
    this.mediaQueryList.removeListener(this.onMediaChange);
    this.sub && this.sub.unsubscribe();
    console.log('EnvelopeComponent unsubscribed router events.');
  }

  private toFirstChildRoute(route) {
    let result = route;
    while (result.firstChild) result = result.firstChild;
    return result;
  }

  private setupFromRouteData(data) {
    this.pageTitle = TITLE_TEXT[Locale.get()][data['title']] || data['title'];
    this.pageType = data['pageType'] || 'normal';
    this.title.setTitle(this.pageTitle);
  }

  private resetScorllTop() {
    if (this.scrollingElement) {
      this.scrollingElement.scrollTop = 0;
      this.scrollTop = 0;
    }
  }

  private setMenuStyle(event?) {
    if (this.layout === 'list') {
      this.menuModal = !this.mediaQueryList.matches;
    } else {
      this.menuModal = true;
    }

    console.log(`MenuList style determined. menuModal = ${this.menuModal}`);
    // FIXME ViewWrappedError will be raised when login -> logout -> login -> resize window...
    event && this.changeDetectorRef.detectChanges();
  }

  private checkPageLayout() {
    let pr: HTMLElement = this.elementRef.nativeElement.querySelector('page-root');
    this.layout = pr.attributes['layout'] ? pr.attributes['layout'].value : 'list';
    console.log(`Layout changed to "${this.layout}".`);
    this.setMenuStyle();
  }


  private _updateScrollTop = () => {
    if (this.scrollingElement) {
      this.scrollTop = this.scrollingElement.scrollTop;
      return;
    }
    if (!this.scrollingElement) {
      let scrollingEl = document.scrollingElement;
      this.scrollingElement = scrollingEl ? scrollingEl : document.documentElement;
    }
  }

  @HostListener('window:scroll', ['$event'])
  onScroll(event) {
    this._updateScrollTop();
  }

  onScrollerClick() {
    let id = setInterval(() => {
      if (this.scrollingElement.scrollTop === 0) {
        clearInterval(id);
        this.scrollTop = 0;
      } else {
        this.scrollingElement.scrollTop = this.scrollingElement.scrollTop - Math.ceil(this.scrollingElement.scrollTop / 10);
      }
    }, 10);
  }

}
