import { Component, OnInit, OnDestroy, inject, DestroyRef, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { trigger, transition, style, animate } from '@angular/animations';
import { RecipeService } from '../../../shared/services/recipe.service';
import { TelegramService } from '../../../core/services/telegram.service';
import { HomeButtonComponent } from '../../../shared/components/home-button/home-button.component';
import { Recipe } from '../../../shared/models';

@Component({
  selector: 'app-recipe-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, HomeButtonComponent],
  templateUrl: './recipe-form.component.html',
  styleUrl: './recipe-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  animations: [
    trigger('slideDown', [
      transition(':enter', [
        style({ opacity: 0, maxHeight: 0, transform: 'translateY(-10px)' }),
        animate('300ms cubic-bezier(0.16, 1, 0.3, 1)', style({ opacity: 1, maxHeight: '100px', transform: 'translateY(0)' }))
      ]),
      transition(':leave', [
        animate('200ms ease', style({ opacity: 0, maxHeight: 0, transform: 'translateY(-10px)' }))
      ])
    ])
  ]
})
export class RecipeFormComponent implements OnInit, OnDestroy {
  isEditMode = false;
  recipeId: number | null = null;
  recipeForm!: FormGroup;

  units = [
    { value: 'GRAM' as const, label: 'г' },
    { value: 'MILLILITER' as const, label: 'мл' },
    { value: 'PIECE' as const, label: 'шт' }
  ];

  private destroyRef = inject(DestroyRef);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private recipeService: RecipeService,
    private telegramService: TelegramService,
    private fb: FormBuilder
  ) {}

  get ingredients(): FormArray {
    return this.recipeForm.get('ingredients') as FormArray;
  }

  ngOnInit() {
    this.recipeForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      photoUrl: [''],
      instructions: ['', Validators.required],
      ingredients: this.fb.array([])
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.recipeId = Number(id);
      this.recipeService.getRecipe(this.recipeId).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (recipe) => {
          this.recipeForm.patchValue({
            name: recipe.name,
            description: recipe.description,
            photoUrl: recipe.photoUrl,
            instructions: recipe.instructions
          });
          recipe.ingredients.forEach(i => {
            this.ingredients.push(this.fb.group({
              name: [i.name, Validators.required],
              quantity: [i.quantity, [Validators.required, Validators.min(0)]],
              unit: [i.unit]
            }));
          });
        },
        error: (err) => {
          console.error('Failed to load recipe', err);
          this.telegramService.showPopup('Ошибка', 'Не удалось загрузить рецепт');
        }
      });
    }

    this.telegramService.showBackButton(() => {
      this.router.navigate(['/recipes']);
    });

    this.telegramService.showMainButton('Сохранить', () => {
      this.saveRecipe();
    });
  }

  ngOnDestroy() {
    this.telegramService.hideBackButton();
    this.telegramService.hideMainButton();
  }

  addIngredient() {
    this.ingredients.push(this.fb.group({
      name: ['', Validators.required],
      quantity: [0, [Validators.required, Validators.min(0)]],
      unit: ['GRAM']
    }));
  }

  removeIngredient(index: number) {
    this.ingredients.removeAt(index);
  }

  addPhoto() {
    this.telegramService.openGallery((photo) => {
      const file = this.dataURLtoFile(photo, 'photo.jpg');
      this.recipeService.uploadPhoto(file).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (response) => {
          this.recipeForm.patchValue({ photoUrl: response.fileId });
        },
        error: (err) => {
          console.error('Failed to upload photo', err);
          this.telegramService.showPopup('Ошибка', 'Не удалось загрузить фото');
        }
      });
    });
  }

  private dataURLtoFile(dataurl: string, filename: string): File {
    const arr = dataurl.split(',');
    const mime = arr[0].match(/:(.*?);/)?.[1] || 'image/jpeg';
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    return new File([u8arr], filename, { type: mime });
  }

  goBack() {
    if (this.isEditMode && this.recipeId) {
      this.router.navigate(['/recipes', this.recipeId]);
    } else {
      this.router.navigate(['/recipes']);
    }
  }

  saveRecipe() {
    if (this.recipeForm.invalid) {
      this.recipeForm.markAllAsTouched();
      return;
    }

    const request = this.recipeForm.value;

    if (this.isEditMode && this.recipeId) {
      this.recipeService.updateRecipe(this.recipeId, request).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => {
          this.router.navigate(['/']);
        },
        error: (err) => {
          console.error('Failed to update recipe', err);
          this.telegramService.showPopup('Ошибка', 'Не удалось обновить рецепт');
        }
      });
    } else {
      this.recipeService.createRecipe(request).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (recipe) => {
          this.router.navigate(['/']);
        },
        error: (err) => {
          console.error('Failed to create recipe', err);
          this.telegramService.showPopup('Ошибка', 'Не удалось создать рецепт');
        }
      });
    }
  }
}
