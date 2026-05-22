import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { PaginaResponse } from '../models/paginacao.models';
import { SalvarVantagemRequest, Vantagem } from '../models/vantagem.models';

@Injectable({ providedIn: 'root' })
export class VantagensService {
  private readonly httpClient = inject(HttpClient);

  listarMinhasVantagens(): Observable<Vantagem[]> {
    return this.httpClient.get<Vantagem[]>(`${API_BASE_URL}/empresas/me/vantagens`);
  }

  listarVantagensDisponiveis(page: number, size: number): Observable<PaginaResponse<Vantagem>> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.httpClient.get<PaginaResponse<Vantagem>>(`${API_BASE_URL}/vantagens`, { params });
  }

  criarVantagem(request: SalvarVantagemRequest): Observable<Vantagem> {
    return this.httpClient.post<Vantagem>(`${API_BASE_URL}/empresas/me/vantagens`, request);
  }

  atualizarVantagem(id: number, request: SalvarVantagemRequest): Observable<Vantagem> {
    return this.httpClient.put<Vantagem>(`${API_BASE_URL}/empresas/me/vantagens/${id}`, request);
  }
}
