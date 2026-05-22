import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { ProfessorResumo } from '../models/professor.models';

@Injectable({ providedIn: 'root' })
export class ProfessoresService {
  private readonly httpClient = inject(HttpClient);
  private readonly saldoProfessorSignal = signal<number | null>(null);

  readonly saldoProfessor = this.saldoProfessorSignal.asReadonly();

  obterMeuResumo(): Observable<ProfessorResumo> {
    return this.httpClient.get<ProfessorResumo>(`${API_BASE_URL}/professores/me/resumo`).pipe(
      tap((resumo) => {
        this.saldoProfessorSignal.set(resumo.saldo);
      }),
    );
  }

  atualizarSaldoProfessor(saldo: number): void {
    this.saldoProfessorSignal.set(saldo);
  }
}
