import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CoupleService } from '../../../shared/services/couple.service';
import { TelegramService } from '../../../core/services/telegram.service';
import { Couple } from '../../../shared/models';

@Component({
  selector: 'app-couple-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './couple-profile.component.html',
  styleUrl: './couple-profile.component.scss'
})
export class CoupleProfileComponent implements OnInit, OnDestroy {
  couple: Couple | null = null;

  constructor(
    private router: Router,
    private coupleService: CoupleService,
    private telegramService: TelegramService
  ) {}

  ngOnInit() {
    this.coupleService.getCouple().subscribe({
      next: (couple) => {
        this.couple = couple;
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

  leaveCouple() {
    if (confirm('Покинуть пару? Все данные будут удалены.')) {
      this.coupleService.leaveCouple().subscribe({
        next: () => {
          this.router.navigate(['/join']);
        }
      });
    }
  }
}
