import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CoupleService } from '../../../shared/services/couple.service';
import { TelegramService } from '../../../core/services/telegram.service';

@Component({
  selector: 'app-couple-join',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './couple-join.component.html',
  styleUrl: './couple-join.component.scss'
})
export class CoupleJoinComponent {
  mode: 'initial' | 'create' | 'join' = 'initial';
  inviteCode = '';
  generatedCode = '';
  error = '';

  constructor(
    private coupleService: CoupleService,
    private telegramService: TelegramService,
    private router: Router
  ) {}

  createCouple() {
    this.coupleService.createCouple().subscribe({
      next: (couple) => {
        this.generatedCode = couple.inviteCode;
        this.mode = 'create';
      },
      error: (err) => {
        this.error = 'Не удалось создать пару';
      }
    });
  }

  joinCouple() {
    if (this.inviteCode.length !== 6) {
      this.error = 'Код должен содержать 6 символов';
      return;
    }

    this.coupleService.joinCouple({ inviteCode: this.inviteCode.toUpperCase() }).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.error = 'Неверный код приглашения';
      }
    });
  }

  shareCode() {
    const url = `https://t.me/your_bot?start=${this.generatedCode}`;
    this.telegramService.share(url, 'Присоединяйся к планировщику питания!');
  }

  showJoinMode() {
    this.mode = 'join';
    this.error = '';
  }

  backToInitial() {
    this.mode = 'initial';
    this.error = '';
    this.inviteCode = '';
  }
}
