import { DocumentSearchPageComponent } from './search/document-search-page/document-search-page.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GlobalSearchPageComponent } from './search/global-search-page/global-search-page.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'portal/home',
    pathMatch: 'full'
  },
  {
    path: 'config',
    loadChildren: 'app/main/content/config/config.module#ConfigModule'
  },
  {
    path: 'portal',
    loadChildren: 'app/main/content/portal/portal.module#PortalModule'
  },
  {
    path: 'admin',
    loadChildren: 'app/main/content/admin/admin.module#AdminModule'
  },
  {
    path: 'search',
    component: GlobalSearchPageComponent,
    data: { title: 'global_search', pageType: 'search' },
  },
  {
    path: 'search/documents',
    component: DocumentSearchPageComponent,
    data: { title: 'document_search', pageType: 'normal' },
  },
  {
    path: 'common',
    loadChildren: 'app/main/content/common/common.module#CommonModule'
  },
  // { path: 'buyer', canActivate: [FeatureGuard], loadChildren: 'app/main/content/buyer/buyer.module#BuyerModule' },
  {
    path: 'supplier',
    loadChildren: 'app/main/content/supplier/supplier.module#SupplierModule'
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ContentRoutingModule { }
