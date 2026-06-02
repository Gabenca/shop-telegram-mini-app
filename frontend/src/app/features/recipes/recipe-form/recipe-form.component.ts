import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { RecipeService } from '../../../shared/services/recipe.service';
import { TelegramService } from '../../../core/services/telegram.service';
import { Recipe, IngredientRequest } from '../../../shared/models';

@Component({
  selector: 'app-recipe-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './recipe-form.component.html',
  styleUrl: './recipe-form.component.scss'
})
export class RecipeFormComponent implements OnInit, OnDestroy {
  isEditMode = false;
  recipeId: number | null = null;

  name = '';
  description = '';
  photoUrl = '';
  instructions = '';
  ingredients: IngredientRequest[] = [];

  units = [
    { value: 'GRAM' as const, label: 'г' },
    { value: 'MILLILITER' as const, label: 'мл' },
    { value: 'PIECE' as const, label: 'шт' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private recipeService: RecipeService,
    private telegramService: TelegramService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.recipeId = Number(id);
      this.recipeService.getRecipe(this.recipeId).subscribe({
        next: (recipe) => {
          this.name = recipe.name;
          this.description = recipe.description;
          this.photoUrl = recipe.photoUrl;
          this.instructions = recipe.instructions;
          this.ingredients = recipe.ingredients.map(i => ({
            name: i.name,
            weightInGrams: i.weightInGrams,
            unit: i.unit
          }));
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
    this.ingredients.push({
      name: '',
      weightInGrams: 0,
      unit: 'GRAM'
    });
  }

  removeIngredient(index: number) {
    this.ingredients.splice(index, 1);
  }

  addPhoto() {
    this.telegramService.openGallery((photo) => {
      const file = this.dataURLtoFile(photo, 'photo.jpg');
      this.recipeService.uploadPhoto(file).subscribe({
        next: (response) => {
          this.photoUrl = response.fileId;
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

  saveRecipe() {
    const request = {
      name: this.name,
      description: this.description,
      photoUrl: this.photoUrl,
      instructions: this.instructions,
      ingredients: this.ingredients
    };

    if (this.isEditMode && this.recipeId) {
      this.recipeService.updateRecipe(this.recipeId, request).subscribe({
        next: () => {
          this.router.navigate(['/recipes', this.recipeId]);
        }
      });
    } else {
      this.recipeService.createRecipe(request).subscribe({
        next: (recipe) => {
          this.router.navigate(['/recipes', recipe.id]);
        }
      });
    }
  }
}
