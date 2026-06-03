import { Component, signal, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { routerTransition } from './app.animations';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
  animations: [routerTransition],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class App {
  protected readonly title = signal('Вкусно вдвоём');
}
