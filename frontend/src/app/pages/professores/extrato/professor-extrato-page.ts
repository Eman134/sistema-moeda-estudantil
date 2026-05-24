import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ExtratoItem } from '../../../models/extrato.models';
import { ExtratoService } from '../../../services/extrato.service';
import { ProfessoresService } from '../../../services/professores.service';

@Component({
  selector: 'app-professor-extrato-page',
  imports: [DatePipe],
  templateUrl: './professor-extrato-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfessorExtratoPage {
  private readonly extratoService = inject(ExtratoService);
  private readonly professoresService = inject(ProfessoresService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly lancamentos = signal<ExtratoItem[]>([]);
  protected readonly saldo = signal<number | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.extratoService
      .consultarExtratoProfessor()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (extrato) => {
          this.lancamentos.set(extrato.lancamentos.content);
          this.saldo.set(extrato.saldo);
          this.professoresService.atualizarSaldoProfessor(extrato.saldo);
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set('Não foi possível carregar o extrato. Tente novamente mais tarde.');
          this.carregando.set(false);
        },
      });
  }
}
