import { HttpInterceptorFn } from '@angular/common/http';

export const telegramInitDataInterceptor: HttpInterceptorFn = (req, next) => {
  const telegramWebApp = (window as any).Telegram?.WebApp;
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
