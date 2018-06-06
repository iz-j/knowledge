import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { LoginPageComponent } from './login-page/login-page.component';
import { PasswordResetReqPageComponent } from './password-reset-page/password-reset-req-page.component';
import { PasswordResetInputPageComponent } from './password-reset-page/password-reset-input-page.component';

import { AuthService } from './auth.service';
import { AuthenticationGurad } from './auth-guard';
import { hogeLibModule } from 'lib/module';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    ReactiveFormsModule,
    hogeLibModule
  ],
  declarations: [
    LoginPageComponent,
    PasswordResetInputPageComponent,
    PasswordResetReqPageComponent,
  ],
  providers: [
    AuthService,
    AuthenticationGurad,
  ]
})
export class AuthModule { }
