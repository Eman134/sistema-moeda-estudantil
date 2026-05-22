import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { AlunoResumo } from '../models/aluno.models';
import { PaginaResponse } from '../models/paginacao.models';

@Injectable({ providedIn: 'root' })
export class AlunosService {
  private readonly httpClient = inject(HttpClient);
  private readonly saldoAlunoSignal = signal<number | null>(null);

  readonly saldoAluno = this.saldoAlunoSignal.asReadonly();

  obterMeuResumo(): Observable<AlunoResumo> {
    return this.httpClient.get<AlunoResumo>(`${API_BASE_URL}/alunos/me/resumo`).pipe(
      tap((resumo) => {
        this.saldoAlunoSignal.set(resumo.saldo);
      }),
    );
  }

  listarAlunos(page: number, size: number, search: string): Observable<PaginaResponse<AlunoResumo>> {
    let params = new HttpParams().set('page', page).set('size', size);

    if (search.trim()) {
      params = params.set('search', search.trim());
    }

    return this.httpClient.get<PaginaResponse<AlunoResumo>>(`${API_BASE_URL}/alunos`, { params });
  }

  atualizarSaldoAluno(saldo: number): void {
    this.saldoAlunoSignal.set(saldo);
  }
}
