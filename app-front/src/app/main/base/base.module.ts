import { CommonModule } from './../content/common/common.module';
import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import { RouterModule } from '@angular/router';

import { EnvelopeModule } from './envelope/envelope.module';

import { NotFoundPageComponent } from './not-found-page/not-found-page.component';

import { ExtendedHttp } from '../../../lib/service/http';//'lib/service/http';
import { AnnouncementService } from '../content/portal/announcement-page/announcement.service';

@NgModule({
  imports: [
    HttpModule,
    RouterModule,
    EnvelopeModule,
  ],
  declarations: [
    NotFoundPageComponent
  ],
  providers: [
    ExtendedHttp,
    AnnouncementService
  ]
})
export class BaseModule { }
