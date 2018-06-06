import { NgModule } from '@angular/core';

import { MainRoutingModule } from './main-routing.module';
import { BaseModule } from './base/base.module';
import { CoreModule } from '../core/core.module';

@NgModule({
  imports: [
    MainRoutingModule,
    BaseModule,
    CoreModule,
  ],
  declarations: [
  ],
  providers: [
  ]
})
export class MainModule { }
