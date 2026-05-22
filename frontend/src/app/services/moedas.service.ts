import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

import { API_BASE_URL } from '../config/api.config';
import { EnviarMoedasRequest, EnviarMoedasResponse } from '../models/moedas.models';
import { ProfessoresService } from './professores.service';

@Injectable({ providedIn: 'root' })
export class MoedasService {
  private readonly httpClient = inject(HttpClient);
  private readonly professoresService = inject(ProfessoresService);

  enviarMoedas(request: EnviarMoedasRequest): Observable<EnviarMoedasResponse> {
    return this.httpClient.post<EnviarMoedasResponse>(`${API_BASE_URL}/moedas/envios`, request).pipe(
      tap((response) => {
        this.professoresService.atualizarSaldoProfessor(response.saldoProfessor);
      }),
    );
  }
}
