import { Component, OnInit, OnDestroy, inject, DestroyRef, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CoupleService } from '../../../shared/services/couple.service';
import { TelegramService } from '../../../core/services/telegram.service';
import { HomeButtonComponent } from '../../../shared/components/home-button/home-button.component';
import { Couple } from '../../../shared/models';

@Component({
  selector: 'app-couple-profile',
  standalone: true,
  imports: [CommonModule, HomeButtonComponent],
  templateUrl: './couple-profile.component.html',
  styleUrl: './couple-profile.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CoupleProfileComponent implements OnInit, OnDestroy {
  couple: Couple | null = null;
  isLoading = signal(true);

  private destroyRef = inject(DestroyRef);

  constructor(
    private router: Router,
    private coupleService: CoupleService,
    private telegramService: TelegramService
  ) {}

  ngOnInit() {
    this.coupleService.getCouple().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (couple) => {
        this.couple = couple;
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load couple', err);
        this.isLoading.set(false);
      }
    });

    this.telegramService.showBackButton(() => {
      this.router.navigate(['/']);
    });
  }

  ngOnDestroy() {
    this.telegramService.hideBackButton();
  }

  shareCode() {
    if (this.couple) {
      const url = `https://t.me/your_bot?start=${this.couple.inviteCode}`;
      this.telegramService.share(url, 'Присоединяйся к планировщику питания!');
    }
  }

  async leaveCouple() {
    const confirmed = await this.telegramService.showConfirm('Подтверждение', 'Покинуть пару? Все данные будут удалены.');
    if (confirmed) {
      this.coupleService.leaveCouple().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.router.navigate(['/join']);
        },
        error: (err) => {
          console.error('Failed to leave couple', err);
          this.telegramService.showPopup('Ошибка', 'Не удалось покинуть пару');
        }
      });
    }
  }
}
