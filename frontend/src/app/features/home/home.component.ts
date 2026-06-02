import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  sections = [
    { icon: '📖', title: 'Рецепты', route: '/recipes' },
    { icon: '📅', title: 'План на неделю', route: '/plan' },
    { icon: '🛒', title: 'Список покупок', route: '/shopping' },
    { icon: '👤', title: 'Профиль пары', route: '/couple' }
  ];

  constructor(private router: Router) {}

  navigateTo(route: string) {
    this.router.navigate([route]);
  }
}
