import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  protected baseUrl = '/api';

  constructor(protected http: HttpClient) {}
}
