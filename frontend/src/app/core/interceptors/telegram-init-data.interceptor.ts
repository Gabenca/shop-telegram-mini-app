import { HttpInterceptorFn } from '@angular/common/http';
import type { WebApp } from '@twa-dev/types';

declare global {
  interface Window {
    Telegram?: {
      WebApp: WebApp;
    };
  }
}

export const telegramInitDataInterceptor: HttpInterceptorFn = (req, next) => {
  const telegramWebApp = window.Telegram?.WebApp;
  if (!telegramWebApp) {
    return next(req);
  }

  const initData = telegramWebApp.initData;

  if (initData) {
    const clonedReq = req.clone({
      setHeaders: {
        'X-Telegram-Init-Data': initData
      }
    });
    return next(clonedReq);
  }

  return next(req);
};
