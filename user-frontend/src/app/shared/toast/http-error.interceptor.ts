import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from './toast.service';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status >= 400 && error.status < 500) {
        toastService.error(error.error?.message || 'Client error occurred');
      } else if (error.status >= 500) {
        toastService.error('Server error occurred. Please try later.');
      }
      return throwError(() => error);
    })
  );
};
