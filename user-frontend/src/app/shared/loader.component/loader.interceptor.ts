import { inject } from '@angular/core';
import { HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { LoaderService } from './loader.service';

export function loaderInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
  const loader = inject(LoaderService);
  loader.show();

  return next(req).pipe(
    finalize(() => loader.hide())
  );
}
