import { Injectable } from '@angular/core';
import { ExtendedHttp } from '../../../../../lib/service/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class MenuListService {

  constructor(private http: ExtendedHttp) { }

  getCounter(featureId: string): Observable<any> {
    return this.http.get('/portal/counter', { featureId: featureId }).map(res => res.json());
  }

  getUnreadAnnounceCounter(): Observable<number> {
    return this.http.get('/portal/announcement/unread/counter').map(res => parseInt(res.text()));
  }
}
