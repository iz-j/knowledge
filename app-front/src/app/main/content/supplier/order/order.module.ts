import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { hogeLibModule } from '../../../../../lib/module';
import { ComponentModule } from '../../../component/component.module';

import { RecievedOrderRoutingModule } from './order-routing.module';
import { ReceivedOrderSharedModule } from '../../../shared/supplier/order/shared.module';

import { ReceivedOrderListPageComponent } from './received-order-list-page/received-order-list-page.component';

import { ReceivedOrderViewPageComponent } from './received-order-view-page/received-order-view-page.component';
import { ReceivedOrderModifyInputPageComponent } from './received-order-modify-input-page/received-order-modify-input-page.component';
import { ReceivedOrderModifyConfirmPageComponent } from './received-order-modify-confirm-page/received-order-modify-confirm-page.component';
import { ReceivedOrderModifyFinishPageComponent } from './received-order-modify-finish-page/received-order-modify-finish-page.component';

import { ReceivedOrderBoardComponent } from './parts/received-order-board/received-order-board.component';
import { ReceivedOrderConfirmationComponent } from './parts/received-order-confirmation/received-order-confirmation.component';
import { ReceivedOrderSheetComponent } from './parts/received-order-sheet/received-order-sheet.component';
import { ReceivedOrderActivitiesSheetComponent } from './parts/received-order-activities-sheet/received-order-activities-sheet.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    hogeLibModule,
    ComponentModule,
    RecievedOrderRoutingModule,
    ReceivedOrderSharedModule,
  ],
  declarations: [
    ReceivedOrderListPageComponent,
    ReceivedOrderViewPageComponent,
    ReceivedOrderModifyInputPageComponent,
    ReceivedOrderModifyConfirmPageComponent,
    ReceivedOrderModifyFinishPageComponent,
    ReceivedOrderBoardComponent,
    ReceivedOrderConfirmationComponent,
    ReceivedOrderSheetComponent,
    ReceivedOrderActivitiesSheetComponent
  ],
  providers: [
  ],
  exports: [
  ]
})
export class ReceivedOrderModule { }
