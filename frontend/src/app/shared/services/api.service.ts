import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  readonly baseUrl = environment.apiUrl;

  constructor(readonly http: HttpClient) {}
}
