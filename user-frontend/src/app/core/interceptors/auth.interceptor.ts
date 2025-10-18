import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  let authReq = req;
  const token = localStorage.getItem('accessToken');

  if (token) {
    authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !authService.isRefreshing) {
        authService.isRefreshing = true;

        return authService.refreshToken().pipe(
          switchMap(res => {
            authService.isRefreshing = false;
            const newReq = req.clone({
              setHeaders: { Authorization: `Bearer ${res.accessToken}` }
            });
            return next(newReq);
          }),
          catchError(err => {
            authService.isRefreshing = false;
            authService.logout();
            return throwError(() => err);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
