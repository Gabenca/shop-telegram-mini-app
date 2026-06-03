import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { TelegramService } from '../services/telegram.service';

export const telegramGuard: CanActivateFn = (route, state) => {
  const telegramService = inject(TelegramService);
  const router = inject(Router);

  if (telegramService.isTelegramWebApp) {
    return true;
  }

  router.navigate(['/no-telegram']);
  return false;
};
