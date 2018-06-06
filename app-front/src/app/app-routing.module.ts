import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { environment } from './../environments/environment';

import { AuthenticationGurad } from './auth/auth-guard';

import { LoginPageComponent } from './auth/login-page/login-page.component';
import { PasswordResetReqPageComponent } from './auth/password-reset-page/password-reset-req-page.component';
import { PasswordResetInputPageComponent } from './auth/password-reset-page/password-reset-input-page.component';


const routes: Routes = [
  {
    path: '',
    redirectTo: '/login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: LoginPageComponent
  },
  {
    path: 'password-reset/request',
    component: PasswordResetReqPageComponent
  },
  {
    path: 'password-reset/input',
    component: PasswordResetInputPageComponent
  },
  {
    path: 'main',
    canActivate: [AuthenticationGurad],
    loadChildren: 'app/main/main.module#MainModule'
  },
];

// Enable library demo pages if not production mode.
if (environment.production === false) {
  routes.push({
    path: 'library-demo',
    loadChildren: 'lib/demo/demo.module#hogeLibDemoModule'
  });
}


// Not match any path, redirect to login page.
routes.push({
  path: '**',
  redirectTo: '/login',
  pathMatch: 'full'
});

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
