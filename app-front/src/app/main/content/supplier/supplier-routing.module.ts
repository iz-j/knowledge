import { FeatureGuard } from './../../service/feature/feature.service';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';


const routes: Routes = [
  {
    path: 'quotation',
    data: { featureId: 'supplier.quotation' },
    canActivateChild: [FeatureGuard],
    loadChildren: 'app/main/content/supplier/quotation/quotation.module#QuotationModule'
  },
  {
    path: 'payment-advice',
    data: { featureId: 'supplier.paymentAdvice' },
    canActivateChild: [FeatureGuard],
    loadChildren: 'app/main/content/supplier/payment-advice/payment-advice.module#PaymentAdviceModule'
  },
  {
    path: 'received-order',
    data: { featureId: 'supplier.receivedOrder' },
    canActivateChild: [FeatureGuard],
    loadChildren: 'app/main/content/supplier/order/order.module#ReceivedOrderModule'
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SupplierRoutingModule { }
