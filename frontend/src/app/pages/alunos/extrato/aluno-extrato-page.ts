import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ExtratoItem } from '../../../models/extrato.models';
import { AlunosService } from '../../../services/alunos.service';
import { ExtratoService } from '../../../services/extrato.service';

@Component({
  selector: 'app-aluno-extrato-page',
  imports: [DatePipe],
  templateUrl: './aluno-extrato-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AlunoExtratoPage {
  private readonly extratoService = inject(ExtratoService);
  private readonly alunosService = inject(AlunosService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly lancamentos = signal<ExtratoItem[]>([]);
  protected readonly saldo = signal<number | null>(null);
  protected readonly carregando = signal(true);
  protected readonly erro = signal<string | null>(null);

  constructor() {
    this.extratoService
      .consultarExtratoAluno()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (extrato) => {
          this.lancamentos.set(extrato.lancamentos.content);
          this.saldo.set(extrato.saldo);
          this.alunosService.atualizarSaldoAluno(extrato.saldo);
          this.carregando.set(false);
        },
        error: () => {
          this.erro.set('Não foi possível carregar o extrato. Tente novamente mais tarde.');
          this.carregando.set(false);
        },
      });
  }
}
