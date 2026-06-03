import { Component, inject, DestroyRef, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CoupleService } from '../../../shared/services/couple.service';
import { TelegramService } from '../../../core/services/telegram.service';
import { HomeButtonComponent } from '../../../shared/components/home-button/home-button.component';

@Component({
  selector: 'app-couple-join',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HomeButtonComponent],
  templateUrl: './couple-join.component.html',
  styleUrl: './couple-join.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CoupleJoinComponent {
  mode: 'initial' | 'create' | 'join' = 'initial';
  generatedCode = '';
  error = '';
  isLoading = signal(false);
  joinForm: FormGroup;

  private destroyRef = inject(DestroyRef);

  constructor(
    private coupleService: CoupleService,
    private telegramService: TelegramService,
    private router: Router,
    private fb: FormBuilder
  ) {
    this.joinForm = this.fb.group({
      inviteCode: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
    });
  }

  createCouple() {
    this.telegramService.hapticFeedback('light');
    this.isLoading.set(true);
    this.coupleService.createCouple().pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (couple) => {
        this.generatedCode = couple.inviteCode;
        this.mode = 'create';
        this.isLoading.set(false);
        this.telegramService.hapticFeedback('medium');
      },
      error: (err) => {
        this.error = 'Не удалось создать пару';
        this.isLoading.set(false);
      }
    });
  }

  joinCouple() {
    if (this.joinForm.invalid) {
      this.joinForm.markAllAsTouched();
      this.error = 'Код должен содержать 6 символов';
      return;
    }

    this.telegramService.hapticFeedback('light');
    this.isLoading.set(true);
    const inviteCode = this.joinForm.value.inviteCode.toUpperCase();
    this.coupleService.joinCouple({ inviteCode }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.router.navigate(['/']);
        this.isLoading.set(false);
        this.telegramService.hapticFeedback('medium');
      },
      error: (err) => {
        this.error = 'Неверный код приглашения';
        this.isLoading.set(false);
      }
    });
  }

  shareCode() {
    this.telegramService.hapticFeedback('light');
    const url = `https://t.me/your_bot?start=${this.generatedCode}`;
    this.telegramService.share(url, 'Присоединяйся к планировщику питания!');
  }

  showJoinMode() {
    this.telegramService.hapticFeedback('light');
    this.mode = 'join';
    this.error = '';
  }

  backToInitial() {
    this.telegramService.hapticFeedback('light');
    this.mode = 'initial';
    this.error = '';
    this.joinForm.reset();
  }

  goHome() {
    this.router.navigate(['/']);
  }
}
