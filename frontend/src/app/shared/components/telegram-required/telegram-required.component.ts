import { Component, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-telegram-required',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="telegram-required">
      <div class="card">
        <h1>Откройте в Telegram</h1>
        <p>Это приложение работает только внутри Telegram Mini App.</p>
        <p class="hint">Зайдите в бота и нажмите кнопку «Открыть приложение».</p>
      </div>
    </div>
  `,
  styles: [`
    .telegram-required {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      padding: 24px;
    }
    .card {
      max-width: 360px;
      padding: 32px 24px;
      background: var(--tg-theme-secondary-bg-color);
      border-radius: var(--radius-xl);
      box-shadow: 0 4px 16px var(--warm-shadow);
      text-align: center;
    }
    h1 {
      font-size: 22px;
      margin-bottom: 12px;
      color: var(--tg-theme-text-color);
    }
    p {
      color: var(--tg-theme-text-color);
      margin-bottom: 8px;
    }
    .hint {
      color: var(--tg-theme-hint-color);
      font-size: 14px;
    }
  `]
})
export class TelegramRequiredComponent {}
