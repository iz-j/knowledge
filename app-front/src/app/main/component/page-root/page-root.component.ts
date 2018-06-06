import { Component, OnInit, Input } from '@angular/core';

type Layout = 'list' | 'detail' | 'simple';
namespace Layout {
  export const LIST: Layout = 'list';
  export const DETAIL: Layout = 'detail';
  export const SIMPLE: Layout = 'simple';
}



/**
 *  Root component of each content pages.
 *
 *  @author iz-j
 */
@Component({
  selector: 'page-root',
  template: `
    <div class="page-root {{layout}}">
      <div class="loading" *ngIf="!ready">
        <hoge-loading></hoge-loading>
      </div>
      <div class="container" [class.loaded]="ready">
        <ng-content></ng-content>
      </div>
    </div>
  `,
  styleUrls: ['./page-root.component.less']
})
export class PageRootComponent implements OnInit {

  @Input() layout: Layout = Layout.LIST;
  @Input() ready: boolean = true;

  constructor() { }

  ngOnInit() { }

}
