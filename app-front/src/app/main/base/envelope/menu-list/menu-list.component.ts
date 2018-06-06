import { AuthService } from './../../../../auth/auth.service';
import { AnnouncementService } from './../../../content/portal/announcement-page/announcement.service';
import { Component, OnInit, OnDestroy, Renderer, Input } from '@angular/core';
import { WindowRef } from '../../../../../lib/base/window-ref';

import { Locale } from './../../../../app.locales';
import { BASE_TEXT } from './../../../../../resources/text/base/base.text';
import { Feature } from '../../../service/feature/feature';
import { FeatureService, FeatureSetCountEvent } from '../../../service/feature/feature.service';
import { MenuListService } from './menu-list.service';
import { Subscription } from 'rxjs/Subscription';
import { APP_VERSION } from './../../../../../environments/version';
import { Router } from '@angular/router';

const EMPTY_FEATURE: Feature = {
  id: '', name: '', url: ''
};

@Component({
  selector: 'menu-list',
  templateUrl: './menu-list.component.html',
  styleUrls: ['./menu-list.component.less']
})
export class MenuListComponent implements OnInit, OnDestroy {
  text: { [key: string]: string; };
  version: string = APP_VERSION;

  @Input()
  set modalMode(value: boolean) {
    this.modal = value;
    this.close();
    this.setMenuFold();
  }

  menuList: Array<any> = [];
  announcementCounter: number;

  modal: boolean;// 詳細画面や画面幅狭い場合はモーダル形式
  opened: boolean;
  opening: boolean;

  subscriptions: Array<Subscription> = [];
  host: boolean;

  constructor(
    private authService: AuthService,
    private featureService: FeatureService,
    private menuListService: MenuListService,
    private renderer: Renderer,
    private windowRef: WindowRef,
    private route: Router,
    private announcementService: AnnouncementService,
  ) { }

  ngOnInit() {
    this.text = BASE_TEXT[Locale.get()];
    this.host = this.authService.claim.host;
    this.menuList = this.featureService.getFeatures()
      .map(f => {
        let fold = this.route.url.indexOf(f.url) < 0;
        return {
          id: f.id,
          name: f.name,
          url: f.url,
          hasCounter: f.hasCounter !== undefined ? f.hasCounter : true,
          subMenus: f.subMenus ? f.subMenus.map(s => { return { name: s.name, id: s.id, url: s.url, counter: 0 }; }) : [],
          fold: fold,
          initialized: false
        };
      });

    this.getUnreadAnnouncementsCounter();
    this.subscriptions.push(this.featureService.refreshCounterRequest.subscribe((featureId) => {
      this.refreshCounter(featureId);
    }));
    this.subscriptions.push(this.featureService.setCountRequest.subscribe((event) => this.setCounter(event)));
  }

  ngOnDestroy() {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  private refreshCounter(featureId) {
    if (!featureId) return;
    console.log(`Counter refresh event has been fired for "${featureId}".`);
    this.menuListService.getCounter(featureId).subscribe(res => {
      console.log(res);
      let menu = this.menuList.find(m => m.id === featureId);
      if (!menu) {
        console.warn(`Menu not found for ${featureId}!`);
        return;
      }
      menu.subMenus.forEach(subMenu => {
        let count = res[subMenu.url];
        if (count === undefined) {
          console.warn(`Counter not found for ${subMenu.url} of ${featureId}!`);
          count = 0;
        }
        subMenu.counter = count;
      });
    });
  }

  private setCounter(event: FeatureSetCountEvent) {
    if (!event) return;
    let featureId = event.id;
    let menu = this.menuList.find(m => m.id === featureId);
    if (!menu) {
      console.warn(`Menu not found for ${featureId}!`);
      return;
    }
    let subMenuId = `${featureId}.${event.subMenuId}`;
    menu = menu.subMenus.find(subMenu => subMenu.id === subMenuId);
    if (!menu) {
      console.warn(`Sub menu not found for ${featureId}.${subMenuId}!`);
      return;
    }
    let result = menu.counter ? event.count !== 0 : menu.counter !== event.count;
    menu.counter = event.count;
    if (event.callback) event.callback(result);
  }

  onExpandableMenuClick(menu) {
    // Update counter at first expanding. After this, counter will be refreshed by the event published by each features.
    if (menu.fold && !menu.initialized && menu.hasCounter) {
      this.featureService.refreshCounter(menu.id);
      menu.initialized = true;
    }
    menu.fold = !menu.fold;
  }

  open() {
    this.opening = true;
    this.opened = true;
    this.setMenuFold();
    this.setScreenScrollable(false);
    setTimeout(() => {
      this.opening = false;
    }, 150);
  }

  close() {
    this.opened = false;
    this.setScreenScrollable(true);
    this.getUnreadAnnouncementsCounter();
  }

  private setScreenScrollable(b: boolean) {
    let body = this.windowRef.nativeWindow.document.body;
    if (b) {
      this.renderer.setElementClass(body, 'scroll-fixed', false);
      body.scrollTop = - body.style.top.replace(/px/, '');
      this.renderer.setElementStyle(body, 'top', null);
    } else {
      this.renderer.setElementStyle(body, 'top', `-${body.scrollTop}px`);
      this.renderer.setElementClass(body, 'scroll-fixed', true);
    }
  }
  private setMenuFold() {
    this.menuList = this.menuList
      .map(f => {
        let fold = this.route.url.indexOf(f.url) < 0;
        f['fold'] = f['fold'] && fold;
        return f;
      });
  }

  private getUnreadAnnouncementsCounter() {
    this.menuListService.getUnreadAnnounceCounter().subscribe(count => {
      this.announcementCounter = count;
      if (count == -1) {
        this.announcementService.updateLastReadTime();
      }
    });
  }

}
