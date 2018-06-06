import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ExtendedHttp } from './../lib/service/http';
import 'rxjs/add/operator/catch';

@Component({
  selector: 'app-errors',
  template: `
    <ul class="app-errors" *ngIf="errors.length > 0">
      <li *ngFor="let error of errors; let i = index;">
        <div class="icon"><i class="wap-icon-warning"></i></div>
        <div class="message">
          <div>SYSTEM ERROR</div>
          <pre>{{error}}</pre>
        </div>
        <i class="clear wap-icon-cancel" (click)="onClearClick(i)"></i>
      </li>
    </ul>
  `,
  styles: [`
    ul.app-errors {
      position: fixed;
      right: 32px;
      top: 80px;
      width: 320px;
    }

    ul.app-errors > li {
      display: flex;
      border-radius: 4px;
      background-color: #e91e63;
      color: #fff;
      padding: 4px;
      margin-bottom: 8px;
      position: relative;
      width: 100%;
      box-sizing: border-box;
    }

    ul.app-errors > li > div.icon {
      width: 16px;
      box-sizing: border-box;
    }

    ul.app-errors > li > div.message {
      width: calc(100% - 16px) ;
      padding-left: 4px;
      box-sizing: border-box;
    }

    ul.app-errors > li > div.message > pre {
      font-size: 12px;
      margin-top: 4px;
      word-break: break-all;
      white-space: pre-wrap;
    }

    ul.app-errors > li > i.clear {
      position: absolute;
      right: 0px;
      top: 0px;
      padding: 4px;
      font-size: 12px;
      opacity: 0.6;
    }

    ul.app-errors > li > i.clear:hover {
      opacity: 1.0;
      cursor: pointer;
    }
  `],
})
export class AppErrors implements OnInit {
  errors: Array<string> = [];

  count = 0;

  constructor(
    private cdr: ChangeDetectorRef,
    private http: ExtendedHttp,
  ) { }

  ngOnInit() { }

  onClearClick(i) {
    this.errors.splice(i, 1);
    this.cdr.detectChanges();
  }

  public push(error: any): void {
    let errMsg: string = null;
    if (error.toString) {
      errMsg = error.toString();
    } else {
      errMsg = error;
    }
    if (errMsg.length > 800) {
      errMsg = errMsg.substr(0, 800) + ' ...';
    }
    this.errors.push(errMsg);

    if (this.errors.length > 5) {
      this.errors.shift();
    }
    this.cdr.detectChanges();


    let errorLog = error.stack ? error.stack : (error.toString ? error.toString() : error);
    this.http.post('/log/error', errorLog).subscribe(() => { });
  }

}
