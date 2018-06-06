import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ReceivedOrderListPageComponent } from './received-order-list-page/received-order-list-page.component';
import { ReceivedOrderViewPageComponent } from './received-order-view-page/received-order-view-page.component';
import { ReceivedOrderModifyInputPageComponent } from './received-order-modify-input-page/received-order-modify-input-page.component';
import { ReceivedOrderModifyConfirmPageComponent } from './received-order-modify-confirm-page/received-order-modify-confirm-page.component';
import { ReceivedOrderModifyFinishPageComponent } from './received-order-modify-finish-page/received-order-modify-finish-page.component';

const routes: Routes = [
  { path: ':status', component: ReceivedOrderListPageComponent, data: { title: 'received_order_management' } },
  { path: 'view/:id/:revision', component: ReceivedOrderViewPageComponent, data: { title: 'received_order_management' } },
  { path: 'finish/:id/:revision', component: ReceivedOrderModifyFinishPageComponent, data: { title: 'received_order_management' } },
  { path: 'decline/finish/:id/:revision', component: ReceivedOrderModifyFinishPageComponent, data: { title: 'received_order_management' } },
  { path: 'modify/input/:id/:revision', component: ReceivedOrderModifyInputPageComponent, data: { title: 'adjust_order' } },
  { path: 'modify/confirm/:id/:revision', component: ReceivedOrderModifyConfirmPageComponent, data: { title: 'adjust_order' } },
  { path: 'modify/finish/:id/:revision', component: ReceivedOrderModifyFinishPageComponent, data: { title: 'adjust_order' } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RecievedOrderRoutingModule { }
