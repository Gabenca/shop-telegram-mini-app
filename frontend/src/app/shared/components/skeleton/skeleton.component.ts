import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'app-skeleton',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="skeleton" [style.width]="width()" [style.height]="height()" [style.border-radius]="radius()"></div>
  `,
  styles: [`
    .skeleton {
      background: linear-gradient(90deg,
        var(--tg-theme-secondary-bg-color) 0%,
        rgba(0, 0, 0, 0.06) 50%,
        var(--tg-theme-secondary-bg-color) 100%);
      background-size: 200% 100%;
      animation: skeleton-shimmer 1.4s ease-in-out infinite;
    }
    @media (prefers-color-scheme: dark) {
      .skeleton {
        background: linear-gradient(90deg,
          var(--tg-theme-secondary-bg-color) 0%,
          rgba(255, 255, 255, 0.06) 50%,
          var(--tg-theme-secondary-bg-color) 100%);
        background-size: 200% 100%;
      }
    }
    @keyframes skeleton-shimmer {
      0% { background-position: 200% 0; }
      100% { background-position: -200% 0; }
    }
  `]
})
export class SkeletonComponent {
  width = input<string>('100%');
  height = input<string>('1em');
  radius = input<string>('var(--radius-sm)');
}
