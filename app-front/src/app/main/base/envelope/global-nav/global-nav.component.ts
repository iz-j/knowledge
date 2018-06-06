import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'global-nav',
  templateUrl: './global-nav.component.html',
  styleUrls: ['./global-nav.component.less']
})
export class GlobalNavComponent implements OnInit, OnDestroy {

  @Input()
  pageTitle: string;
  @Input()
  pageType: string;// portal, normal, search

  env: string;
  showEnv: boolean = true;

  constructor(
    private router: Router
  ) { }

  ngOnInit() {
    this.env = environment.name;
  }

  ngOnDestroy() {
  }

  onGlobalSearchClick(referer) {
    this.router.navigate(['/main/search'], { queryParams: { referer: referer } });
  }

}
