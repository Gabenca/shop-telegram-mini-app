import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Couple, JoinCoupleRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class CoupleService {

  private couple$ = new BehaviorSubject<Couple | null>(null);

  constructor(private api: ApiService) {}

  getCoupleObservable(): Observable<Couple | null> {
    return this.couple$.asObservable();
  }

  getCouple(): Observable<Couple> {
    return this.api.http.get<Couple>(`${this.api.baseUrl}/couple`).pipe(
      tap(couple => this.couple$.next(couple))
    );
  }

  createCouple(): Observable<Couple> {
    return this.api.http.post<Couple>(`${this.api.baseUrl}/couple/create`, {}).pipe(
      tap(couple => this.couple$.next(couple))
    );
  }

  joinCouple(request: JoinCoupleRequest): Observable<Couple> {
    return this.api.http.post<Couple>(`${this.api.baseUrl}/couple/join`, request).pipe(
      tap(couple => this.couple$.next(couple))
    );
  }

  leaveCouple(): Observable<void> {
    return this.api.http.delete<void>(`${this.api.baseUrl}/couple/leave`).pipe(
      tap(() => this.couple$.next(null))
    );
  }
}
