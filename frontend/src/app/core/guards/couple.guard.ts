import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { CoupleService } from '../../shared/services/couple.service';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

export const coupleGuard: CanActivateFn = (route, state) => {
  const coupleService = inject(CoupleService);
  const router = inject(Router);

  return coupleService.getCouple().pipe(
    map(couple => {
      if (couple) {
        return true;
      }
      router.navigate(['/join']);
      return false;
    }),
    catchError(() => {
      router.navigate(['/join']);
      return of(false);
    })
  );
};
