import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { ExtratoResponse } from '../models/extrato.models';

@Injectable({ providedIn: 'root' })
export class ExtratoService {
  private readonly httpClient = inject(HttpClient);

  consultarExtratoAluno(page = 0, size = 20): Observable<ExtratoResponse> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.httpClient.get<ExtratoResponse>(`${API_BASE_URL}/alunos/me/extrato`, { params });
  }

  consultarExtratoProfessor(page = 0, size = 20): Observable<ExtratoResponse> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.httpClient.get<ExtratoResponse>(`${API_BASE_URL}/professores/me/extrato`, { params });
  }
}
