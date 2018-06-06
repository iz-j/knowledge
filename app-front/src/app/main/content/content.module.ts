import { NgModule } from '@angular/core';
import { ContentRoutingModule } from './content-routing.module';
import { SearchModule } from './search/search.module';

@NgModule({
  imports: [
    ContentRoutingModule,
    SearchModule,
  ],
  declarations: [
  ],
  providers: [
  ]
})
export class ContentModule { }