import { Pipe, PipeTransform } from '@angular/core';

type Unit = 'GRAM' | 'MILLILITER' | 'PIECE';

@Pipe({
  name: 'unit',
  standalone: true
})
export class UnitPipe implements PipeTransform {
  private labels: Record<Unit, string> = {
    GRAM: 'г',
    MILLILITER: 'мл',
    PIECE: 'шт'
  };

  transform(value: Unit): string {
    return this.labels[value] ?? value;
  }
}
