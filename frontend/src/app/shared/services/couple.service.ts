import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Couple, JoinCoupleRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class CoupleService extends ApiService {

  private couple$ = new BehaviorSubject<Couple | null>(null);

  getCoupleObservable(): Observable<Couple | null> {
    return this.couple$.asObservable();
  }

  getCouple(): Observable<Couple> {
    return this.http.get<Couple>(`${this.baseUrl}/couple`).pipe(
      tap(couple => this.couple$.next(couple))
    );
  }

  createCouple(): Observable<Couple> {
    return this.http.post<Couple>(`${this.baseUrl}/couple/create`, {}).pipe(
      tap(couple => this.couple$.next(couple))
    );
  }

  joinCouple(request: JoinCoupleRequest): Observable<Couple> {
    return this.http.post<Couple>(`${this.baseUrl}/couple/join`, request).pipe(
      tap(couple => this.couple$.next(couple))
    );
  }

  leaveCouple(): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/couple/leave`).pipe(
      tap(() => this.couple$.next(null))
    );
  }
}
