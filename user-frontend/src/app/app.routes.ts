import { RouterModule, Routes } from '@angular/router';
import { RegisterComponent } from './auth/register/register.component';
import { LoginComponent } from './auth/login/login.component';
import { ForgotPasswordComponent } from './auth/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './auth/reset-password/reset-password.component';
import { VerifyEmailComponent } from './auth/verify-email/verify-email.component';
import { NgModule } from '@angular/core';
import { AuthGuard } from './core/guards/auth.guard';
import { UserManagementComponent } from './admin/user.management.component';
import { MainLayoutComponent } from './shared/layout/main-layout.component';
import { GuestGuard } from './core/guards/guest.guard';


export const routes: Routes = [
  { path: 'register', component: RegisterComponent, canActivate: [GuestGuard] },
  { path: 'login', component: LoginComponent, canActivate: [GuestGuard] },
  { path: 'forgot-password', component: ForgotPasswordComponent, canActivate: [GuestGuard] },
  { path: 'reset-password', component: ResetPasswordComponent, canActivate: [GuestGuard] },
  { path: 'verify', component: VerifyEmailComponent, canActivate: [GuestGuard] },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'allUsers', component: UserManagementComponent, data: { roles: ['ADMIN'] } },
      { path: 'citizen', loadChildren: () => import('./issue/routes/citizen/citizen.routes').then(m => m.citizenRoutes), canActivate: [AuthGuard]},
      { path: 'staff', loadChildren: () => import('./issue/routes/staff/staff.routes').then(m => m.staffRoutes), canActivate: [AuthGuard]}
    ]
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
