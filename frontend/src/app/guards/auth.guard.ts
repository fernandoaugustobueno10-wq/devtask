
import { inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.verificarSessao().pipe(
    map((autenticado) => {
      if (autenticado) {
        return true;
      }

      return router.createUrlTree(['/login']);
    }),
    catchError((erro: unknown) => {
      if (
        erro instanceof HttpErrorResponse &&
        (erro.status === 401 || erro.status === 403)
      ) {
        return of(router.createUrlTree(['/login']));
      }

      return of(router.createUrlTree(['/login']));
    })
  );
};
