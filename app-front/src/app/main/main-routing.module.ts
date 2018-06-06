import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { EnvelopeComponent } from './base/envelope/envelope.component';
import { NotFoundPageComponent } from './base/not-found-page/not-found-page.component';

const routes: Routes = [
  {
    path: '',
    component: EnvelopeComponent,
    loadChildren: 'app/main/content/content.module#ContentModule'
  },
  {
    path: '**',
    component: NotFoundPageComponent
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MainRoutingModule { }
