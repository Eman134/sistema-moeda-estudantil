import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { AlunosService } from '../../../services/alunos.service';
import { DadosMockService } from '../../../services/dados-mock.service';

@Component({
  selector: 'app-aluno-home-page',
  templateUrl: './aluno-home-page.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AlunoHomePage {
  private readonly dadosMockService = inject(DadosMockService);
  private readonly alunosService = inject(AlunosService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly saldoAluno = this.alunosService.saldoAluno;
  protected readonly beneficiosDisponiveis = this.dadosMockService.beneficiosDisponiveis;

  constructor() {
    this.alunosService.obterMeuResumo().pipe(takeUntilDestroyed(this.destroyRef)).subscribe();
  }
}
