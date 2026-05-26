import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';

import { Vantagem } from '../../../models/vantagem.models';
import { AlunosService } from '../../../services/alunos.service';
import { ToastService } from '../../../services/toast.service';
import { VantagensService } from '../../../services/vantagens.service';

@Component({
  selector: 'app-aluno-home-page',
  templateUrl: './aluno-home-page.html',
  styleUrl: './aluno-home-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AlunoHomePage {
  private readonly alunosService = inject(AlunosService);
  private readonly vantagensService = inject(VantagensService);
  private readonly toastService = inject(ToastService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly saldoAluno = this.alunosService.saldoAluno;
  protected readonly beneficiosDisponiveis = signal<Vantagem[]>([]);
  protected readonly carregandoBeneficios = signal(false);
  protected readonly totalBeneficios = signal(0);
  protected readonly totalPaginas = signal(0);
  protected readonly paginaAtual = signal(0);
  protected readonly tamanhoPagina = signal(6);
  protected readonly resgatandoVantagemId = signal<number | null>(null);

  protected readonly paginaExibida = computed(() => this.paginaAtual() + 1);
  protected readonly podeVoltarPagina = computed(() => this.paginaAtual() > 0 && !this.carregandoBeneficios());
  protected readonly podeAvancarPagina = computed(
    () => this.paginaAtual() + 1 < this.totalPaginas() && !this.carregandoBeneficios(),
  );

  constructor() {
    this.carregarResumoAluno();
    this.carregarBeneficios();
  }

  protected paginaAnterior(): void {
    if (!this.podeVoltarPagina()) {
      return;
    }

    this.paginaAtual.update((pagina) => pagina - 1);
    this.carregarBeneficios();
  }

  protected proximaPagina(): void {
    if (!this.podeAvancarPagina()) {
      return;
    }

    this.paginaAtual.update((pagina) => pagina + 1);
    this.carregarBeneficios();
  }

  protected podeResgatar(beneficio: Vantagem): boolean {
    const saldo = this.saldoAluno() ?? 0;

    return beneficio.custoMoedas <= saldo && this.resgatandoVantagemId() === null;
  }

  protected iniciarResgate(beneficio: Vantagem): void {
    if (!this.podeResgatar(beneficio)) {
      this.toastService.aviso('Saldo insuficiente', 'Você ainda não possui moedas suficientes para esta vantagem.');
      return;
    }

    this.resgatandoVantagemId.set(beneficio.id);
    this.vantagensService
      .resgatarVantagem(beneficio.id)
      .pipe(
        finalize(() => this.resgatandoVantagemId.set(null)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (resgate) => {
          this.alunosService.atualizarSaldoAluno(resgate.saldoAtual);
          this.toastService.sucesso(
            'Vantagem resgatada',
            `Cupom ${resgate.codigoCupom} gerado. Confira seu e-mail com o QRCode.`,
          );
        },
        error: (erro: unknown) => {
          this.toastService.erro('Erro ao resgatar vantagem', this.extrairMensagemErro(erro));
        },
      });
  }

  protected estaResgatando(beneficio: Vantagem): boolean {
    return this.resgatandoVantagemId() === beneficio.id;
  }

  protected formatarMoedas(valor: number): string {
    return `${valor} moedas`;
  }

  private carregarResumoAluno(): void {
    this.alunosService
      .obterMeuResumo()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        error: (erro: unknown) => {
          this.toastService.erro('Erro ao carregar saldo', this.extrairMensagemErro(erro));
        },
      });
  }

  private carregarBeneficios(): void {
    this.carregandoBeneficios.set(true);
    this.vantagensService
      .listarVantagensDisponiveis(this.paginaAtual(), this.tamanhoPagina())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (pagina) => {
          this.beneficiosDisponiveis.set(pagina.content);
          this.totalBeneficios.set(pagina.totalElements);
          this.totalPaginas.set(pagina.totalPages);
          this.carregandoBeneficios.set(false);
        },
        error: (erro: unknown) => {
          this.carregandoBeneficios.set(false);
          this.toastService.erro('Erro ao carregar vantagens', this.extrairMensagemErro(erro));
        },
      });
  }

  private extrairMensagemErro(erro: unknown): string {
    if (typeof erro === 'object' && erro !== null && 'error' in erro) {
      const httpError = erro as { error?: unknown };
      if (typeof httpError.error === 'string') {
        return httpError.error;
      }
    }

    return 'Não foi possível completar a operação. Tente novamente.';
  }
}
