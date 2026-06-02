import { Injectable } from '@angular/core';
import type { PopupButton, WebApp } from '@twa-dev/types';

declare global {
  interface Window {
    Telegram?: {
      WebApp: WebApp;
    };
  }
}

@Injectable({
  providedIn: 'root'
})
export class TelegramService {

  private get webApp(): WebApp | null {
    return window.Telegram?.WebApp ?? null;
  }

  get isTelegramWebApp(): boolean {
    return !!this.webApp;
  }

  constructor() {
    if (this.isTelegramWebApp) {
      this.webApp!.ready();
      this.webApp!.expand();
    }
  }

  getMainButton() {
    return this.webApp!.MainButton;
  }

  getBackButton() {
    return this.webApp!.BackButton;
  }

  showMainButton(text: string, onClick: () => void) {
    if (!this.isTelegramWebApp) return;
    const mainButton = this.getMainButton();
    mainButton.setText(text);
    mainButton.onClick(onClick);
    mainButton.show();
  }

  hideMainButton() {
    if (!this.isTelegramWebApp) return;
    this.getMainButton().hide();
  }

  showBackButton(onClick: () => void) {
    if (!this.isTelegramWebApp) return;
    const backButton = this.getBackButton();
    backButton.onClick(onClick);
    backButton.show();
  }

  hideBackButton() {
    if (!this.isTelegramWebApp) return;
    this.getBackButton().hide();
  }

  openCamera(callback: (photo: string) => void) {
    if (!this.isTelegramWebApp) return;
    const wa = this.webApp as any;
    wa.openCamera({
      capture: 'camera'
    }, (photo: string) => {
      if (photo) {
        callback(photo);
      }
    });
  }

  openGallery(callback: (photo: string) => void) {
    if (!this.isTelegramWebApp) return;
    const wa = this.webApp as any;
    wa.openGallery({
      multiple: false
    }, (photo: string) => {
      if (photo) {
        callback(photo);
      }
    });
  }

  share(url: string, text?: string) {
    if (!this.isTelegramWebApp) return;
    this.webApp!.openTelegramLink(`https://t.me/share/url?url=${encodeURIComponent(url)}${text ? '&text=' + encodeURIComponent(text) : ''}`);
  }

  hapticFeedback(type: 'light' | 'medium' | 'heavy' | 'rigid' | 'soft' = 'light') {
    if (!this.isTelegramWebApp) return;
    this.webApp!.HapticFeedback.impactOccurred(type);
  }

  showPopup(title: string, message: string, buttons: PopupButton[] = [{ type: 'ok' }]) {
    if (!this.isTelegramWebApp) return;
    this.webApp!.showPopup({
      title,
      message,
      buttons
    });
  }

  close() {
    if (!this.isTelegramWebApp) return;
    this.webApp!.close();
  }
}
